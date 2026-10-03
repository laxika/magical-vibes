package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "KHC", collectorNumber = "51")
public class MiaraThornOfTheGlade extends Card {

    private static final MayPayManaEffect DRAW_FOR_MANA_AND_LIFE = new MayPayManaEffect(
            "{1}", 1, new DrawCardEffect(1), "Pay {1} and 1 life to draw a card?");

    public MiaraThornOfTheGlade() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new TriggeringCardConditionalEffect(
                new CardSubtypePredicate(CardSubtype.ELF), DRAW_FOR_MANA_AND_LIFE));
        addEffect(EffectSlot.ON_DEATH, DRAW_FOR_MANA_AND_LIFE);
    }
}
