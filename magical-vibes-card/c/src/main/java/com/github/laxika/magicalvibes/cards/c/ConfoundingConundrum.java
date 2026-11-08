package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.PermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "53")
public class ConfoundingConundrum extends Card {

    public ConfoundingConundrum() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.OPPONENT_CONTROLS),
                TriggerOptions.DEFAULT.withInterveningIf(new PermanentEnteredThisTurn(
                        new CardTypePredicate(CardType.LAND), 2, CountScope.TARGET_PLAYER)),
                new ReturnPermanentControlledByPlayerToHandEffect(new PermanentIsLandPredicate(), "land"));
    }
}
