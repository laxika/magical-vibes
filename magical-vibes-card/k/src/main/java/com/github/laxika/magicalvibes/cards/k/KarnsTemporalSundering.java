package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.ExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DOM", collectorNumber = "55")
public class KarnsTemporalSundering extends Card {

    public KarnsTemporalSundering() {
        // Target player takes an extra turn after this one
        target(1, 1)
                .addSpellEffect(new ExtraTurnEffect(1));
        // Return up to one target nonland permanent to its owner's hand
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsLandPredicate()),
                "Target must be a nonland permanent"
        ), 0, 1)
                .addSpellEffect(ReturnToHandEffect.target());
        // Exile Karn's Temporal Sundering
        addSpellEffect(new ExileSpellEffect());
    }
}
