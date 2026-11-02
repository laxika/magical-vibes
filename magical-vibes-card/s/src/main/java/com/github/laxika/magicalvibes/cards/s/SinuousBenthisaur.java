package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "76")
public class SinuousBenthisaur extends Card {

    public SinuousBenthisaur() {
        PermanentCount cavesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER);
        CardsInGraveyard caveCardsInYourGraveyard = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER);

        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                        new Sum(cavesYouControl, caveCardsInYourGraveyard), 2));
    }
}
