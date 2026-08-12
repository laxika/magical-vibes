package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.service.input.MayMiscHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Accept → the matching top card goes onto the battlefield; decline → it stays on top of the library.
 */
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPutMatchingOntoBattlefieldHandler implements MayEffectHandlerBean {

    private final MayMiscHandlerService mayMiscHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPutMatchingOntoBattlefieldEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        LookAtTopCardMayPutMatchingOntoBattlefieldEffect effect =
                (LookAtTopCardMayPutMatchingOntoBattlefieldEffect) ability.effects().getFirst();
        mayMiscHandlerService.handleLookAtTopCardPutLandOrCreatureChoice(
                gameData, player, accepted, effect.enterTapped());
    }
}
