package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "144")
public class ResoundingRoar extends Card {

    public ResoundingRoar() {
        // Target creature gets +3/+3 until end of turn. Targeting auto-derived from the effect's TargetSpec.
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));

        // Cycling {5}{R}{G}{W} ({5}{R}{G}{W}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, target creature gets +6/+6 until end of turn." The reflexive trigger rides
        // on the cycling ability: its target creature is chosen at activation, the +6/+6 resolves, then the
        // cycling draw resumes. Targeting is auto-derived from BoostTargetCreatureEffect's TargetSpec.
        addHandActivatedAbility(new ActivatedAbility(false, "{5}{R}{G}{W}",
                List.of(new BoostTargetCreatureEffect(6, 6), new DrawCardEffect(1)),
                "Cycling {5}{R}{G}{W} ({5}{R}{G}{W}, Discard this card: Draw a card.)"));
    }
}
