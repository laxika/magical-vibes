package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Max;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DRC", collectorNumber = "9")
@CardRegistration(set = "DRC", collectorNumber = "25")
public class ProphetOfTheScarab extends Card {

    public ProphetOfTheScarab() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect(new Max(
                new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER),
                new CardsInGraveyard(
                        new CardSubtypePredicate(CardSubtype.ZOMBIE), CountScope.CONTROLLER))));
    }
}
