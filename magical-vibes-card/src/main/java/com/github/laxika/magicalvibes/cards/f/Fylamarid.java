package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMP", collectorNumber = "64")
public class Fylamarid extends Card {

    public Fylamarid() {
        // Flying is auto-loaded from Scryfall.
        // This creature can't be blocked by blue creatures.
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentColorInPredicate(Set.of(CardColor.BLUE))));

        // {U}: Target creature becomes blue until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false, "{U}",
                List.of(new GrantColorUntilEndOfTurnEffect(CardColor.BLUE)),
                "{U}: Target creature becomes blue until end of turn.",
                TargetFilters.creature()
        ));
    }
}
