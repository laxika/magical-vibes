package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastOrActivateDuringYourTurnEffect;

/**
 * Grand Abolisher: during your turn, your opponents can't cast spells or activate abilities of
 * artifacts, creatures, or enchantments.
 */
@CardRegistration(set = "M12", collectorNumber = "19")
@CardRegistration(set = "BIG", collectorNumber = "2")
public class GrandAbolisher extends Card {

    public GrandAbolisher() {
        addEffect(EffectSlot.STATIC, new OpponentsCantCastOrActivateDuringYourTurnEffect());
    }
}
