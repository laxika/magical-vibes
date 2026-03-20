package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.KickerReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "DOM", collectorNumber = "163")
public class GiftOfGrowth extends Card {

    public GiftOfGrowth() {
        // Kicker {2}
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));

        // Untap target creature.
        // It gets +2/+2 until end of turn.
        // If this spell was kicked, that creature gets +4/+4 until end of turn instead.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new UntapTargetPermanentEffect())
                .addEffect(EffectSlot.SPELL, new KickerReplacementEffect(
                        new BoostTargetCreatureEffect(2, 2),
                        new BoostTargetCreatureEffect(4, 4)
                ));
    }
}
