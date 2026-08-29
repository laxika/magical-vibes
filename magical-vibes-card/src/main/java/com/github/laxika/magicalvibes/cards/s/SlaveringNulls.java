package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "WWK", collectorNumber = "92")
public class SlaveringNulls extends Card {

    public SlaveringNulls() {
        // Whenever this creature deals combat damage to a player, if you control a Swamp,
        // you may have that player discard a card.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ConditionalEffect(
                new ControlsPermanent(new PermanentHasSubtypePredicate(CardSubtype.SWAMP)),
                new MayEffect(
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER),
                        "Have that player discard a card?"
                )
        ));
    }
}
