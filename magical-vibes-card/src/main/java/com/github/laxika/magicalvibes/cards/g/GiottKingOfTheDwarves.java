package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "FIN", collectorNumber = "223")
public class GiottKingOfTheDwarves extends Card {

    public GiottKingOfTheDwarves() {
        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(new CardSubtypePredicate(CardSubtype.DWARF), loot()));
        addEffect(EffectSlot.ON_ALLY_EQUIPMENT_ENTERS_BATTLEFIELD, loot());
    }

    private static MayEffect loot() {
        return new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?");
    }
}
