package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "72")
public class TheSpiritOasis extends Card {

    public TheSpiritOasis() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DrawCardEffect(new PermanentCount(
                        new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER)));
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.SHRINE),
                        new DrawCardEffect(1)));
    }
}
