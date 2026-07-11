package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SOS", collectorNumber = "156")
public class OraclesRestoration extends Card {

    public OraclesRestoration() {
        // Target creature you control gets +1/+1 until end of turn. You draw a card and gain 1 life.
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control"
        )).addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(1, 1))
          .addEffect(EffectSlot.SPELL, new DrawCardEffect())
          .addEffect(EffectSlot.SPELL, new GainLifeEffect(1));
    }
}
