package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "76")
public class SinuousBenthisaur extends Card {

    public SinuousBenthisaur() {
        PermanentCount cavesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER);
        CardsInGraveyard caveCardsInYourGraveyard = new CardsInGraveyard(
                new CardSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER);

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                        new Sum(cavesYouControl, caveCardsInYourGraveyard), 2));
    }
}
