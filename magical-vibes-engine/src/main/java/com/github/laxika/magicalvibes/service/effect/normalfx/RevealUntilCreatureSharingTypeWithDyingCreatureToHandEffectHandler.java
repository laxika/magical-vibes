package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateRestOnBottomRandomEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSharesCreatureTypeWithSourcePredicate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Heirloom Blade's snapshot-relative creature-type library reveal. */
@Component
@RequiredArgsConstructor
public class RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffectHandler
        implements NormalEffectHandlerBean {

    private final RevealUntilCardPredicateRestOnBottomRandomEffectHandler revealHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (RevealUntilCreatureSharingTypeWithDyingCreatureToHandEffect) effect;
        if (typedEffect.dyingCreature() == null) {
            return;
        }
        revealHandler.resolveUsingSourcePermanentSnapshot(gameData, entry,
                new RevealUntilCardPredicateRestOnBottomRandomEffect(
                        new CardSharesCreatureTypeWithSourcePredicate(),
                        LibrarySearchDestination.HAND),
                typedEffect.dyingCreature());
    }
}
