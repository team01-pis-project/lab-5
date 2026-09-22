import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

//Лабораторная работа №5

public class Gost28195Reliability {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        // Чтение чисел из файла
        String fileName = (args.length > 0) ? args[0] : "gost_data.txt";
        List<Double> nums = readNumbers(fileName);
        if (nums == null || nums.size() < 11) {
            System.err.println("Ошибка чтения: " + fileName);
            return;
        }

        int idx = 0;
        int Q = nums.get(idx++).intValue();           // число отказов
        int N = nums.get(idx++).intValue();           // число экспериментов
        double TV_MIN = nums.get(idx++);              // нижняя граница Tv
        double TV_MAX = nums.get(idx++);              // верхняя граница Tv
        double TV_DOP = nums.get(idx++);              // допустимое время восстановления
        double TP_MIN = nums.get(idx++);              // нижняя граница Tp
        double TP_MAX = nums.get(idx++);              // верхняя граница Tp
        double TP_DOP = nums.get(idx++);              // допустимое время преобразования
        double BASE = nums.get(idx++);                // базовое значение критерия
        int SAMPLE_TV = nums.get(idx++).intValue();   // размер выборки Tv
        int SAMPLE_TP = nums.get(idx++).intValue();   // размер выборки Tp

        System.out.println("Данные из файла: " + fileName);
        Random rnd = new Random(42);

        // Рассчитаем оценочный элемент Н0401: вероятность безотказной работы
        // P = 1 - Q/N
        double e0401 = 1.0 - (double) Q / N;
        System.out.printf("Вероятность безотказной работы: Н0401 (P = 1 - Q/N) = 1 - %d/%d = %.6f%n", Q, N, e0401);

        // Рассчитываем оценочный элемент Н0501: по среднему времени восстановления
        // Генерируем выборку Tv ~ U[0.7; 1.2] и берём среднее
        double sumTv = 0;
        for (int i = 0; i < SAMPLE_TV; i++) {
            sumTv += TV_MIN + (TV_MAX - TV_MIN) * rnd.nextDouble();
        }
        double tvMean = sumTv / SAMPLE_TV;
        // По ГОСТ: 1, если Tv <= Tv_dop; иначе Tv_dop / Tv
        double e0501 = (tvMean <= TV_DOP) ? 1.0 : TV_DOP / tvMean;
        System.out.printf("Среднее Tv (выборка %d) = %.6f с%n", SAMPLE_TV, tvMean);
        System.out.printf("Оценку по среднему времени восстановления: Н0501 = %.6f%n", e0501);

        // Рассчитываем оценочный элемент Н0502: по времени преобразования
        // Для каждого значения: 1, если Tp <= Tp_dop; иначе Tp_dop / Tp; затем среднее
        double sumE0502 = 0;
        for (int i = 0; i < SAMPLE_TP; i++) {
            double tp = TP_MIN + (TP_MAX - TP_MIN) * rnd.nextDouble();
            double ei = (tp <= TP_DOP) ? 1.0 : TP_DOP / tp;
            sumE0502 += ei;
        }
        double e0502 = sumE0502 / SAMPLE_TP;
        System.out.printf("Оценка по среднему времени преобразования: Н0502 (выборка %d) = %.6f%n%n", SAMPLE_TP, e0502);

        // Метрики (формула 3)
        // Метрика 1 — только Н0401
        double M1 = e0401;
        System.out.printf("Метрика M1 (по Н0401) = %.6f%n", M1);

        // Метрика 2 — среднее Н0501 и Н0502 (формула 3)
        double M2 = (e0501 + e0502) / 2.0;
        System.out.printf("Метрика M2 (по Н0501 и Н0502) = (%.6f + %.6f)/2 = %.6f%n", e0501, e0502, M2);

        // Абсолютные показатели критериев (формула 4)
        double K = (M1 + M2) / 2.0;
        System.out.printf("Абсолютный показатель K = (M1 + M2)/2 = %.6f%n", K);

        // Относительные показатели (формула 5)
        double Krel = K / BASE;
        System.out.printf("Относительный показатель K' = K/%.2f = %.6f%n", BASE, Krel);

        // Фактор надёжности (формула 6)
        double R = Krel*1;
        System.out.printf("Фактор надёжности R = %.6f%n", R);
    }

    // Чтение всех чисел из текстового файла
    private static List<Double> readNumbers(String fileName) {
        List<Double> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                for (String p : line.split("[,\\s]+")) {
                    if (!p.isEmpty()) {
                        list.add(Double.parseDouble(p.replace(',', '.')));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            return null;
        }
        return list;
    }
}