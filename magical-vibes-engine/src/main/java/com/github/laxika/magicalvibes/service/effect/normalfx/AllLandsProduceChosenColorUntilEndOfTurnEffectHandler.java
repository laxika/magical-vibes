package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AllLandsProduceChosenColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AllLandsProduceChosenColorUntilEndOfTurnEffect} (Hall of Gemstone): pauses to let
 * the player whose upkeep it is choose a color. That player is the stack entry's {@code targetId}
 * (the active player for an {@code EACH_UPKEEP_TRIGGERED} trigger), not the source's controller.
 * Recording the color runs in {@code ChoiceHandlerService.handleAllLandsProduceChosenColorChoice}.
 */
@Component
@RequiredArgsConstructor
public class AllLandsProduceChosenColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AllLandsProduceChosenColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID choosingPlayerId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        playerInputService.beginAllLandsProduceChosenColorChoice(gameData, choosingPlayerId);
    }
}
