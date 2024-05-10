package com.linkcao.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="key_info")
public class KeyInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String keyValue;
    @Column(nullable = false,columnDefinition = "VARCHAR(50) DEFAULT 'https://api.openai.com'")
    private String api;
    // 描述这个Key干嘛的 可空
    private String description;

}
