package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "36")
public class InkDissolver extends Card {

    public InkDissolver() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, each opponent
        // mills three cards.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new MillEffect(3, MillRecipient.EACH_OPPONENT))));
    }
}
