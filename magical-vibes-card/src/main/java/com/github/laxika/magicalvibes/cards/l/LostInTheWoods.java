package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardRemoveTargetFromCombatIfMatchEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

@CardRegistration(set = "DKA", collectorNumber = "123")
public class LostInTheWoods extends Card {

    public LostInTheWoods() {
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new RevealTopCardRemoveTargetFromCombatIfMatchEffect(new CardSubtypePredicate(CardSubtype.FOREST)));
    }
}
