package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "BOK", collectorNumber = "80")
public class ScourgeOfNumai extends Card {

    public ScourgeOfNumai() {
        // At the beginning of your upkeep, you lose 2 life if you don't control an Ogre.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, new PermanentHasSubtypePredicate(CardSubtype.OGRE)),
                new LoseLifeEffect(2)));
    }
}
