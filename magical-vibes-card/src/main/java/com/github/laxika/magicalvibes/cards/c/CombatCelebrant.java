package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;

@CardRegistration(set = "AKH", collectorNumber = "125")
public class CombatCelebrant extends Card {

    public CombatCelebrant() {
        // Exert: "You may exert this creature as it attacks. When you do, untap all other creatures
        // you control and after this phase, there is an additional combat phase." Modeled as an
        // optional attack trigger (matching Glory-Bound Initiate). Choosing to exert also keeps the
        // creature tapped through its next untap step.
        addEffect(EffectSlot.ON_ATTACK, new MayEffect(
                SequenceEffect.of(
                        new UntapPermanentsEffect(TapUntapScope.OTHER_CONTROLLED_CREATURES),
                        new AdditionalCombatMainPhaseEffect(1),
                        new SkipNextUntapEffect(TapUntapScope.SELF)
                ),
                "Exert Combat Celebrant as it attacks? (Untap all other creatures you control and take an additional combat phase.)"
        ));
    }
}
