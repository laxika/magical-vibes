package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "110")
public class ResoundingThunder extends Card {

    public ResoundingThunder() {
        // Resounding Thunder deals 3 damage to any target. (DealDamageToAnyTargetEffect's
        // TargetSpec narrows the legal target to any target — no explicit filter needed.)
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));

        // Cycling {5}{B}{R}{G} ({5}{B}{R}{G}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, it deals 6 damage to any target." The reflexive trigger rides on the
        // cycling ability: its target is chosen at activation, the 6 damage resolves, then the cycling draw
        // resumes. Targeting is auto-derived from DealDamageToAnyTargetEffect's TargetSpec.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{B}{R}{G}",
                List.of(new DealDamageToAnyTargetEffect(6), new DrawCardEffect(1)),
                "Cycling {5}{B}{R}{G} ({5}{B}{R}{G}, Discard this card: Draw a card.)"));
    }
}
