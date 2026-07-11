package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReconnectionService {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    public void resendAwaitingInput(GameData gameData, UUID playerId) {
        interactionHandlerRegistry.replayPrompt(gameData, playerId);
    }
}
