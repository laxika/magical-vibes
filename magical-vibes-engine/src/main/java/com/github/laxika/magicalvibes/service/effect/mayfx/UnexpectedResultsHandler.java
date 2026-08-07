package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UnexpectedResultsEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Unexpected Results — the land branch only; the nonland branch is queued as a
 * {@code RevealTopCardMayPlayFreeEffect} and handled by its own handler.
 */
@Component
@RequiredArgsConstructor
public class UnexpectedResultsHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UnexpectedResultsEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayMiscHandlerService.handleUnexpectedResultsLandChoice(gameData, player, accepted, ability);
    }
}
