package org.example;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/numbers")
@Tag(name = "Number Controller", description = "Контроллер для поиска N-ного минимального числа в XLSX файле")
public class NumberController {

    @Autowired
    private NumberService numberService;

    @PostMapping("/find-min")
    @Operation(summary = "Найти N-ное минимальное число в XLSX файле")
    public ResponseEntity<?> findNthMinimalNumber(
            @Parameter(description = "Путь к локальному XLSX файлу")
            @RequestParam String filePath,
            @Parameter(description = "Порядковый номер минимального числа (N)")
            @RequestParam int n) {

        try {
            int result = numberService.findNthMinimalNumber(filePath, n);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Ошибка при обработке файла: " + e.getMessage());
        }
    }
}
