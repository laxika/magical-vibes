package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.KinshipEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "MOR", collectorNumber = "69")
public class NightshadeSchemers extends Card {

    public NightshadeSchemers() {
        // Kinship — At the beginning of your upkeep, you may look at the top card of your library.
        // If it shares a creature type with this creature, you may reveal it. If you do, each opponent
        // loses 2 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new KinshipEffect(List.of(
                new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT))));
    }
}
