package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameUpdate {

    private MessageType type;
    private Long priorityPlayerId;
    private TurnStep currentStep;
    private Long activePlayerId;
    private Integer turnNumber;
    private String logEntry;
}
