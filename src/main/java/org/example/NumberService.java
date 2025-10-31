package org.example;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.PriorityQueue;

@Service
public class NumberService {
    private static final boolean USE_QUICK_SELECT = true; // или false для Max-Heap

    public int findNthMinimalNumber(String filePath, int n) throws IOException {
        int[] numbers = readNumbersFromExcel(filePath);

        if (numbers.length == 0) {
            throw new IllegalArgumentException("Массив не может быть пустым");
        }
        if (n <= 0 || n > numbers.length) {
            throw new IllegalArgumentException("N должно быть в диапазоне от 1 до " + numbers.length);
        }

        if (USE_QUICK_SELECT) {
            return findNthMinimalWithQuickSelect(numbers, n);
        } else {
            return findNthMinimalWithMaxHeap(numbers, n);
        }
    }

    /**
     * Получение массива чисел из файла xlsx
     */
    private int[] readNumbersFromExcel(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("Файл не существует: " + filePath);
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Указанный путь ведет к папке, а не к файлу: " + filePath);
        }
        if (!filePath.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Файл должен быть в формате .xlsx");
        }

        try (FileInputStream fileInputStream = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fileInputStream)
        ) {
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            int[] numbers = new int[rowCount];
            int validNumberCount = 0;

            // Можно улучшить находя столбец и строку с числами автоматически
            // ИЗМЕНИТЬ для другой строки
            for (int i = 0; i < rowCount; i++) { // начинаем с 0 строки
                Row row = sheet.getRow(i);
                if (row != null) {
                    // ИЗМЕНИТЬ для другого столбца
                    Cell cell = row.getCell(0); // 0 = A, 1 = B, 2 = C
                    if (cell != null && cell.getCellType() == CellType.NUMERIC) {
                        numbers[validNumberCount++] = (int) cell.getNumericCellValue();
                    }
                }
            }

            // Возвращаем массив только с валидными числами
            if (validNumberCount < numbers.length) {
                int[] trimmedNumbers = new int[validNumberCount];
                System.arraycopy(numbers, 0, trimmedNumbers, 0, validNumberCount);
                return trimmedNumbers;
            }

            return numbers;
        }
    }

    /**
     * Эффективный алгоритм поиска N-ного минимального числа
     * Используем max-heap размера N для хранения N минимальных чисел
     * Временная сложность: O(M log N), где M - количество чисел, N - размер кучи
     * Пространственная сложность: O(N)
     */
    private int findNthMinimalWithMaxHeap(int[] numbers, int n) {
        // Max-heap для хранения N минимальных чисел
        PriorityQueue<Integer> maxHeap = new PriorityQueue<>(n, Collections.reverseOrder());

        for (int number : numbers) {
            if (maxHeap.size() < n) {
                maxHeap.offer(number);
            } else if (number < maxHeap.peek()) {
                // Если текущее число меньше максимального в куче, заменяем
                maxHeap.poll();
                maxHeap.offer(number);
            }
        }

        // Верхний элемент кучи - N-ное минимальное число
        return maxHeap.peek();
    }

    /**
     * Альтернативная реализация с использованием QuickSelect
     * Средняя временная сложность: O(M), в худшем случае O(M^2)
     */
    private int findNthMinimalWithQuickSelect(int[] numbers, int n) {

        // Создаем копию массива чтобы не менять оригинал
        int[] arr = numbers.clone();
        int k = n - 1; // преобразуем n (1-based) в индекс (0-based)

        return quickSelect(arr, 0, arr.length - 1, k);
    }

    /**
     * Рекурсивный метод QuickSelect
     */
    private int quickSelect(int[] arr, int left, int right, int k) {
        // Защита от бесконечной рекурсии
        if (left < 0 || right >= arr.length || left > right) {
            throw new IllegalStateException("Некорректные индексы в quickSelect");
        }

        if (left == right) {
            return arr[left];
        }

        // Выбираем случайный опорный элемент и разделяем массив
        int pivotIndex = randomizedPartition(arr, left, right);

        // Определяем в какой части искать
        if (k == pivotIndex) {
            return arr[k]; // Нашли нужный элемент!
        } else if (k < pivotIndex) {
            // Ищем в левой части
            return quickSelect(arr, left, pivotIndex - 1, k);
        } else {
            // Ищем в правой части
            return quickSelect(arr, pivotIndex + 1, right, k);
        }
    }

    /**
     * Разделение массива со случайным опорным элементом
     */
    private int randomizedPartition(int[] arr, int left, int right) {
        // Выбираем случайный индекс между left и right
        int randomIndex = left + (int) (Math.random() * (right - left + 1));

        // Перемещаем случайный элемент в конец для использования как опорный
        swap(arr, randomIndex, right);

        return partition(arr, left, right);
    }

    /**
     * Стандартное разделение массива относительно опорного элемента
     */
    private int partition(int[] arr, int left, int right) {
        int pivot = arr[right]; // опорный элемент (последний)
        int i = left; // индекс меньшего элемента

        for (int j = left; j < right; j++) {
            // Если текущий элемент меньше или равен опорному
            if (arr[j] <= pivot) {
                // Перемещаем меньшие элементы влево
                swap(arr, i, j);
                i++;
            }
        }
        // Ставим опорный элемент на правильную позицию
        swap(arr, i, right);
        return i; // Возвращаем индекс опорного элемента
    }

    /**
     * Вспомогательный метод для обмена элементов
     */
    private void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
