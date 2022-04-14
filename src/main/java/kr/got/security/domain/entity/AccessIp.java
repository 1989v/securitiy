package kr.got.security.domain.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
@Table(name = "ACCESS_IP")
@Entity
public class AccessIp implements Serializable {
    @Id
    @GeneratedValue
    @Column(name = "IP_ID", unique = true, nullable = false)
    private Long id;

    @Column(name = "IP_ADRESS", nullable = false)
    private String ipAddress;
}
