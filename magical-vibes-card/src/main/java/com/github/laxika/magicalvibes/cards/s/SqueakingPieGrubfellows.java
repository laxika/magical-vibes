package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "78")
public class SqueakingPieGrubfellows extends Card {

    public SqueakingPieGrubfellows() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, each opponent
        // discards a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new DiscardEffect(1, DiscardRecipient.EACH_OPPONENT, false))));
    }
}
