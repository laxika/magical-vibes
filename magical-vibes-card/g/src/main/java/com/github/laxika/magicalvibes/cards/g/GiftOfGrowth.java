package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DOM", collectorNumber = "163")
public class GiftOfGrowth extends Card {

    public GiftOfGrowth() {
        // Kicker {2}
        addStaticEffect(new KickerEffect("{2}"));

        // Untap target creature.
        // It gets +2/+2 until end of turn.
        // If this spell was kicked, that creature gets +4/+4 until end of turn instead.
        target(TargetFilters.creature()).addSpellEffect(new UntapPermanentsEffect(Subjects.target()))
                .addSpellEffect(new ConditionalReplacementEffect(new Kicked(), 
                        BoostEffect.target(2, 2),
                        BoostEffect.target(4, 4)
                ));
    }
}
