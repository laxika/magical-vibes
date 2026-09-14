package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HA4", collectorNumber = "20")
public class AbominationOfLlanowar extends Card {

    public AbominationOfLlanowar() {
        PermanentCount elvesYouControl =
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER);
        CardsInGraveyard elfCardsInGraveyard =
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.ELF), CountScope.CONTROLLER);
        Sum elfCount = new Sum(elvesYouControl, elfCardsInGraveyard);
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(elfCount, elfCount));
    }
}
