package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "116")
public class JundSojourners extends Card {

    public JundSojourners() {
        // When this creature dies, it deals 1 damage to any target. Modelled as a mandatory targeted
        // death trigger (Pitchburn Devils / Esper Sojourners cycle pattern); the "any target" default
        // covers creatures, players and planeswalkers.
        addEffect(EffectSlot.ON_DEATH, new DealDamageToAnyTargetEffect(1));

        // Cycling {2}{R} ({2}{R}, Discard this card: Draw a card.) — discard cost is intrinsic.
        // "When you cycle this card, it deals 1 damage to any target." The reflexive cycle trigger rides
        // on the cycling ability (Esper Sojourners pattern): the any-target is chosen at activation, the
        // ping resolves, then the cycling draw resumes. The target is derived from the damage effect.
        addHandActivatedAbility(new ActivatedAbility(false, "{2}{R}",
                List.of(new DealDamageToAnyTargetEffect(1), new DrawCardEffect(1)),
                "Cycling {2}{R} ({2}{R}, Discard this card: Draw a card.)"));
    }
}
