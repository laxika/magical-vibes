package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffectHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles the optional battlefield placement after the library reveal has found a match. */
@Component
@RequiredArgsConstructor
public class RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestHandler
        implements MayEffectHandlerBean {

    private final RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffectHandler effectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCardPredicateMayPutOntoBattlefieldShuffleRestEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        effectHandler.completeMayChoice(gameData, player, accepted, ability);
    }
}
