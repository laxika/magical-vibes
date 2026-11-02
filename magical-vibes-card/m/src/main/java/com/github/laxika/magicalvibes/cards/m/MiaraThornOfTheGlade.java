package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHC", collectorNumber = "51")
public class MiaraThornOfTheGlade extends Card {

    private static final MayPayManaEffect DRAW_FOR_MANA_AND_LIFE = new MayPayManaEffect(
            "{1}", 1, new DrawCardEffect(1), "Pay {1} and 1 life to draw a card?");

    public MiaraThornOfTheGlade() {
        addTrigger(Triggers.creatureDies(
                        new PermanentHasSubtypePredicate(CardSubtype.ELF),
                        ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                DRAW_FOR_MANA_AND_LIFE);
    }
}
