package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "36")
public class StaveOff extends Card {

    public StaveOff() {
        // Target creature gains protection from the color of your choice until end of turn.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
