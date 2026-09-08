package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "LCI", collectorNumber = "139")
public class CalamitousCaveIn extends Card {

    public CalamitousCaveIn() {
        var damage = new Sum(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER),
                new CardsInGraveyard(new CardSubtypePredicate(CardSubtype.CAVE), CountScope.CONTROLLER));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(damage, false, true, null));
    }
}
