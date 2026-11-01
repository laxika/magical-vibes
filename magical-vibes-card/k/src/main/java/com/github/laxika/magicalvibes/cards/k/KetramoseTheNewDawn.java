package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.condition.CardsInExileAtLeast;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "209")
public class KetramoseTheNewDawn extends Card {

    public KetramoseTheNewDawn() {
        addStaticEffect(new CantAttackOrBlockUnlessEffect(
                new CardsInExileAtLeast(7),
                "there are seven or more cards in exile"
        ));
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.GRAVEYARD, Zone.BATTLEFIELD),
                Triggers.NONTOKEN, ObjectRelation.ANY).onlyDuringYourTurn()), TriggerOptions.ONE_OR_MORE,
                SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1)));
    }
}
