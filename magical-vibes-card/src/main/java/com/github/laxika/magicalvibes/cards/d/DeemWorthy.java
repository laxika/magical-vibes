package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "127")
public class DeemWorthy extends Card {

    public DeemWorthy() {
        // Deem Worthy deals 7 damage to target creature. (DealDamageToTargetCreatureEffect's
        // TargetSpec narrows the legal target to a creature — no explicit filter needed.)
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(7));

        // Cycling {3}{R} ({3}{R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, you may have it deal 2 damage to target creature." The reflexive
        // trigger rides on the cycling ability: the optional creature target is chosen at activation
        // (minTargets 0 lets the controller decline and still cycle), the 2 damage resolves, then the
        // cycling draw resumes. Targeting is auto-derived from DealDamageToTargetCreatureEffect's TargetSpec.
        addHandActivatedAbility(new ActivatedAbility(false, "{3}{R}",
                List.of(new DealDamageToTargetCreatureEffect(2), new DrawCardEffect(1)),
                "Cycling {3}{R} ({3}{R}, Discard this card: Draw a card.)",
                null, null, null, null, List.of(), 0, 1));
    }
}
