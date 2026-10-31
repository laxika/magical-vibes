package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "180")
@CardRegistration(set = "MAR", collectorNumber = "35")
@CardRegistration(set = "OMB", collectorNumber = "35")
@CardRegistration(set = "CMM", collectorNumber = "296")
@CardRegistration(set = "C20", collectorNumber = "178")
@CardRegistration(set = "NEC", collectorNumber = "119")
@CardRegistration(set = "SCD", collectorNumber = "191")
public class HuntersInsight extends Card {

    public HuntersInsight() {
        // Delayed trigger watching the chosen creature (controlled by the spell's controller even if the
        // creature changes controller); EventValue reads the damage dealt so that many cards are drawn.
        target(TargetFilters.creatureYouControl())
                .addSpellEffect(new GrantEffectToTargetUntilEndOfTurnEffect(
                        EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER,
                        new DrawCardEffect(new EventValue()), true));
    }
}
