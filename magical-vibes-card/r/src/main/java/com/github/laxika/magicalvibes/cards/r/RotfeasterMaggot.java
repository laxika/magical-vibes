package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "112")
@CardRegistration(set = "IMA", collectorNumber = "105")
public class RotfeasterMaggot extends Card {

    public RotfeasterMaggot() {
        // When this creature enters, exile target creature card from a graveyard. You gain life
        // equal to that card's toughness.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetCreatureCardFromGraveyardGainLifeEqualToToughnessEffect());
    }
}
