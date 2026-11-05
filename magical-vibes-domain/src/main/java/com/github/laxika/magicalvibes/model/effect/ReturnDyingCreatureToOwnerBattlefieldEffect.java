package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Returns a dying creature card from its owner's graveyard to the battlefield, applying persistent
 * subtype, keyword, and counter riders as it enters. The default return is under the owner's
 * control; {@code returnUnderController} supports effects that say "under your control".
 *
 * <p>The dying card is the stack entry's triggering card; a card that left the graveyard and came back after the
 * ability triggered is a new object (CR 400.7) and is not returned.
 */
public record ReturnDyingCreatureToOwnerBattlefieldEffect(
        CounterType enterWithCounter,
        int enterWithCounterCount,
        CardSubtype grantSubtype,
        Set<Keyword> grantKeywords,
        boolean enterTapped,
        boolean returnUnderController,
        List<CardSubtype> subtypeOverride
) implements CardEffect {

    public ReturnDyingCreatureToOwnerBattlefieldEffect() {
        this(null, 0, null, Set.of(), false, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords) {
        this(enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords, false, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords,
                                                       boolean enterTapped) {
        this(enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords, enterTapped, false, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect(CounterType enterWithCounter,
                                                       int enterWithCounterCount,
                                                       CardSubtype grantSubtype,
                                                       Set<Keyword> grantKeywords,
                                                       boolean enterTapped,
                                                       boolean returnUnderController) {
        this(enterWithCounter, enterWithCounterCount, grantSubtype, grantKeywords,
                enterTapped, returnUnderController, List.of());
    }

    public ReturnDyingCreatureToOwnerBattlefieldEffect {
        grantKeywords = grantKeywords == null ? Set.of() : Set.copyOf(grantKeywords);
        subtypeOverride = subtypeOverride == null ? List.of() : List.copyOf(subtypeOverride);
    }
}
