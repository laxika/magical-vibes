package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect;

import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "232")
public class TaskmasterMercenaryMimic extends Card {

    public TaskmasterMercenaryMimic() {
        target(0, 1).addEffect(
                EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new BecomeCopyOfTargetCreatureOrGraveyardCreatureCardUntilYourNextTurnEffect(
                        "Taskmaster, Mercenary Mimic",
                        Set.of(CardSubtype.HUMAN, CardSubtype.VILLAIN),
                        Set.of(CardType.CREATURE),
                        Set.of(CardSupertype.LEGENDARY),
                        Set.of()));
    }
}
