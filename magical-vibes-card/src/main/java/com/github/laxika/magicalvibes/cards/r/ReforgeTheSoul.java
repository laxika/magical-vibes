package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MiracleCast;
import com.github.laxika.magicalvibes.model.effect.DiscardHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

@CardRegistration(set = "INR", collectorNumber = "167")
public class ReforgeTheSoul extends Card {

    public ReforgeTheSoul() {
        // Miracle {1}{R}
        addCastingOption(new MiracleCast("{1}{R}"));

        // Each player discards their hand, then draws seven cards.
        addEffect(EffectSlot.SPELL, new DiscardHandEffect(DiscardRecipient.EACH_PLAYER));
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
    }
}
