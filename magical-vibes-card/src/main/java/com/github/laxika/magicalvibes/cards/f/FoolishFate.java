package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RiderRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SOS", collectorNumber = "83")
public class FoolishFate extends Card {

    public FoolishFate() {
        // Destroy target creature. Infusion — if you gained life this turn, that creature's
        // controller also loses 3 life. The upgraded branch destroys and drains in one effect
        // so both paths share the single creature target.
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        )).addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new GainedLifeThisTurn(),
                new DestroyTargetPermanentEffect(),
                new DestroyTargetPermanentThenEffect(new LoseLifeEffect(3), RiderRecipient.TARGET_CONTROLLER)));
    }
}
