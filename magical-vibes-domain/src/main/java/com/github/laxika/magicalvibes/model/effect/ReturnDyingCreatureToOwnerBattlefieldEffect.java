package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;
import java.util.UUID;

/**
 * Returns a dying creature card from its owner's graveyard to the battlefield under its owner's
 * control, applying persistent subtype, keyword, and counter riders as it enters.
 */
public record ReturnDyingCreatureToOwnerBattlefieldEffect(
        UUID dyingCardId,
        CounterType enterWithCounter,
        int enterWithCounterCount,
        CardSubtype grantSubtype,
        Set<Keyword> grantKeywords
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToOwnerBattlefieldEffect() {
        this(null, null, 0, null, Set.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords) {
        this(null, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords);
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect {
        grantKeywords = grantKeywords == null ? Set.of() : Set.copyOf(grantKeywords);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToOwnerBattlefieldEffect(
                dyingCardId, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords);
    }
}
