package org.example.app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TransferResponseDto {
    private long cardIdFrom;
    private long cardIdTo;
    private long amount;
    private String status;
    private String reason;
}
