package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Believe — may put the looked-at creature onto the battlefield; on decline, put it into hand.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandHandler
        implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        mayMiscHandlerService.handleLookAtTopCardMayPutCreatureElseToHandChoice(gameData, player, accepted);
    }
}
