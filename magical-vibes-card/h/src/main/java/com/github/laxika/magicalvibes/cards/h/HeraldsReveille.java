package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerControlledPermanentExploredThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "YLCI", collectorNumber = "5")
public class HeraldsReveille extends Card {

    public HeraldsReveille() {
        ControllerControlledPermanentExploredThisTurn explored =
                new ControllerControlledPermanentExploredThisTurn();
        addEffect(EffectSlot.SPELL, new ConditionalEffect(explored,
                new SeekLibraryEffect(1, new CardSubtypePredicate(CardSubtype.MERFOLK))));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(new NotCondition(explored),
                new DrawCardEffect(1)));
    }
}
