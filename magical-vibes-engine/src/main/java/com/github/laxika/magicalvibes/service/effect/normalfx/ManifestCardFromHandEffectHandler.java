package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MarkChosenPermanentAsManifestedEffect;
import com.github.laxika.magicalvibes.model.effect.ManifestCardFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

/** Resolves manifesting a card chosen from the controller's hand. */
@Component
@RequiredArgsConstructor
public class ManifestCardFromHandEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ManifestCardFromHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PutCardToBattlefieldEffect putEffect = new PutCardToBattlefieldEffect(
                new CardTruePredicate(), "card", false, false, false, false,
                false, false, false, false, true, 2, 2, Set.of(CardType.CREATURE));
        playerInteractionSupport.applyPutCardToBattlefield(
                gameData, entry.getControllerId(), putEffect, entry.getXValue(), entry.getEventValue(),
                null, entry.getCard().getId(), new MarkChosenPermanentAsManifestedEffect(), null,
                null, ignored -> true, entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot());
    }
}
