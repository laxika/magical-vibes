package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "142")
public class ArchonOfTheTriumvirate extends Card {

    public ArchonOfTheTriumvirate() {
        // Whenever this creature attacks, detain up to two target nonland permanents your opponents control.
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 2)
                .addEffect(EffectSlot.ON_ATTACK, new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN, TargetCategory.PERMANENT));
    }
}
