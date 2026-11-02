package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardRemoveTargetFromCombatIfMatchEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "123")
public class LostInTheWoods extends Card {

    public LostInTheWoods() {
        addTrigger(Triggers.creatureAttacksYouOrYourPlaneswalker(),
                new RevealTopCardRemoveTargetFromCombatIfMatchEffect(new CardSubtypePredicate(CardSubtype.FOREST)));
    }
}
