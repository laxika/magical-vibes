package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "96")
public class PyroclastConsul extends Card {

    public PyroclastConsul() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, this
        // creature deals 2 damage to each creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new MassDamageEffect(2))));
    }
}
