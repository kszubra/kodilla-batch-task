package com.kodilla.task.batch.kodillabatchtask;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserOutput {
    private String name;
    private String surname;
    private Integer ageInYears;
}
