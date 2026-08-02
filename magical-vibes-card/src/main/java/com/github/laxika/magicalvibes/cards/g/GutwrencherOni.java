package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCountAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "CHK", collectorNumber = "113")
public class GutwrencherOni extends Card {

    public GutwrencherOni() {
        // At the beginning of your upkeep, discard a card if you don't control an Ogre.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCountAtMost(0, new PermanentHasSubtypePredicate(CardSubtype.OGRE)),
                new DiscardEffect(1, DiscardRecipient.CONTROLLER)));
    }
}
