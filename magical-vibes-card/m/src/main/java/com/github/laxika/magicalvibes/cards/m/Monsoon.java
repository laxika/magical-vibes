package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPlayersPermanentsAndDamageEqualToCountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "298")
public class Monsoon extends Card {

    public Monsoon() {
        // At the beginning of each player's end step, tap all untapped Islands that player
        // controls and this enchantment deals X damage to the player, where X is the number
        // of Islands tapped this way.
        addEffect(EffectSlot.END_STEP_TRIGGERED,
                new TapPlayersPermanentsAndDamageEqualToCountEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.ISLAND)));
    }
}
