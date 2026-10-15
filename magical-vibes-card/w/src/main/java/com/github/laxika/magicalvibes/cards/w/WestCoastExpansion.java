package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SpellXAtLeast;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "MSC", collectorNumber = "40")
@CardRegistration(set = "MSC", collectorNumber = "345")
public class WestCoastExpansion extends Card {

    public WestCoastExpansion() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(new XValue()));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new SpellXAtLeast(5),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardSubtypePredicate(CardSubtype.HERO))));
    }
}
