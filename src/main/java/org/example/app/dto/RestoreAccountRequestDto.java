package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RestoreAccountRequestDto {
    private String username;
    private String restoreCode;
    private String newPassword;
}
