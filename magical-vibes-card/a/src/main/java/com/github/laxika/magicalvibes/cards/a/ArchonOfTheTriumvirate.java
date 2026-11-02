package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "142")
@CardRegistration(set = "GK2", collectorNumber = "7")
public class ArchonOfTheTriumvirate extends Card {

    public ArchonOfTheTriumvirate() {
        // Whenever this creature attacks, detain up to two target nonland permanents your opponents control.
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 2)
                .addTrigger(Triggers.attacks(), new LockTargetPermanentEffect(
                        true, true, true, EffectDuration.UNTIL_YOUR_NEXT_TURN, TargetPredicates.permanent()));
    }
}
