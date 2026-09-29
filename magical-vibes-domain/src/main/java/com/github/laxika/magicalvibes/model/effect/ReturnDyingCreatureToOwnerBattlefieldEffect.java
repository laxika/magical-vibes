package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Returns a dying creature card from its owner's graveyard to the battlefield, applying persistent
 * subtype, keyword, and counter riders as it enters. The default return is under the owner's
 * control; {@code returnUnderController} supports effects that say "under your control".
 */
public record ReturnDyingCreatureToOwnerBattlefieldEffect(
        UUID dyingCardId,
        CounterType enterWithCounter,
        int enterWithCounterCount,
        CardSubtype grantSubtype,
        Set<Keyword> grantKeywords,
        boolean enterTapped,
        boolean returnUnderController,
        List<CardSubtype> subtypeOverride
) implements CardEffect, DyingCreatureCardAwareEffect {

    public ReturnDyingCreatureToOwnerBattlefieldEffect() {
        this(null, null, 0, null, Set.of(), false, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords) {
        this(null, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords, false, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords,
                                                       boolean enterTapped) {
        this(null, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords, enterTapped, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords,
                                                       boolean enterTapped,
                                                       boolean returnUnderController) {
        this(null, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords,
                enterTapped, returnUnderController, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords,
                                                       boolean enterTapped,
                                                       boolean returnUnderController,
                                                       List<CardSubtype> subtypeOverride) {
        this(null, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords,
                enterTapped, returnUnderController, subtypeOverride);
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect {
        grantKeywords = grantKeywords == null ? Set.of() : Set.copyOf(grantKeywords);
        subtypeOverride = subtypeOverride == null ? List.of() : List.copyOf(subtypeOverride);
    }

    @Override
    public CardEffect boundToDyingCard(UUID dyingCardId) {
        return new ReturnDyingCreatureToOwnerBattlefieldEffect(
                dyingCardId, enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords,
                enterTapped, returnUnderController, subtypeOverride);
    }
}
