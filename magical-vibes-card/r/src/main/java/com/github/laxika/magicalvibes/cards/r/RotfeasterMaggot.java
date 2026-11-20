package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "112")
@CardRegistration(set = "IMA", collectorNumber = "105")
public class RotfeasterMaggot extends Card {

    public RotfeasterMaggot() {
        // When this creature enters, exile target creature card from a graveyard. You gain life
        // equal to that card's toughness.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(
                        1, new CardTypePredicate(CardType.CREATURE), false),
                new GainLifeEffect(AmountThisWay.exiledToughness()));
    }
}
