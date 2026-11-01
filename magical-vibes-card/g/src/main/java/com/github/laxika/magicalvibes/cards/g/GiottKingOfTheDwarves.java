package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "223")
@CardRegistration(set = "FIN", collectorNumber = "488")
public class GiottKingOfTheDwarves extends Card {

    public GiottKingOfTheDwarves() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DWARF), loot()));
        addTrigger(Triggers.enters(Triggers.EQUIPMENT, ObjectRelation.ANOTHER_YOU_CONTROL), loot());
    }

    private static MayEffect loot() {
        return new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?");
    }
}
