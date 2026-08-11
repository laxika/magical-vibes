package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accepting puts the matching top card onto the battlefield; declining puts it into hand.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandHandler
        implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect effect = ability.effects().stream()
                .filter(LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect.class::isInstance)
                .map(LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect.class::cast)
                .findFirst()
                .orElseThrow();
        mayMiscHandlerService.handleLookAtTopCardMayPutMatchingElseToHandChoice(
                gameData, player, accepted, effect.tapped());
    }
}
