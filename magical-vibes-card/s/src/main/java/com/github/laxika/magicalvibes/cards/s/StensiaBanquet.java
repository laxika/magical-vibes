package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "EMN", collectorNumber = "144")
public class StensiaBanquet extends Card {

    public StensiaBanquet() {
        addSpellEffect(DealDamageEffect.toTargetOpponentOrPlaneswalker(new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.VAMPIRE), CountScope.CONTROLLER)));
        addSpellEffect(new DrawCardEffect(1));
    }
}
