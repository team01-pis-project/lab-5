import java.util.Locale;
import java.util.Random;

//Лабораторная работа №5

public class Gost28195Reliability {

    // Задаем входные данные
    static final int Q = 5;                    // число отказов
    static final int N = 1000;                 // число экспериментов
    static final double TV_MIN = 0.7;          // нижняя граница Tv
    static final double TV_MAX = 1.2;          // верхняя граница Tv
    static final double TV_DOP = 0.85;         // допустимое время восстановления
    static final double TP_MIN = 9.0;          // нижняя граница времени преобразования
    static final double TP_MAX = 14.0;         // верхняя граница
    static final double TP_DOP = 12.0;         // допустимое время преобразования
    static final double BASE = 0.95;           // базовое значение критериев
    static final int SAMPLE_TV = 100;          // размер выборки Tv
    static final int SAMPLE_TP = 200;          // размер выборки Tp

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
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

        // Метрики (формула 3): среднее ОЭ (равные веса)
        double metricWork = (e0401 + e0501 + e0502) / 3.0;
        System.out.printf("Метрика работоспособности M = (Н0401 + Н0501 + Н0502)/3 = %.6f%n", metricWork);

        // Абсолютные показатели критериев (формула 4)
        double kAbsWork = metricWork;
        System.out.printf("Абсолютный показатель «Работоспособность» K2 = %.6f%n", kAbsWork);

        // Относительные показатели (формула 5)
        double kRelWork = kAbsWork / BASE;
        System.out.printf("Относительный «Работоспособность» K2/Kб = %.6f%n", kRelWork);

        // Фактор надёжности (формула 6)
        double factor = (kRelWork) / 1.0;
        System.out.printf("Фактор надёжности R = %.6f%n", factor);
    }
}