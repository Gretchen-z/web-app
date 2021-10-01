package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferResponseDto {
    private String cardNumFrom;
    private String cardNumTo;
    private long amount;
    private String status;
    private String reason;
}
