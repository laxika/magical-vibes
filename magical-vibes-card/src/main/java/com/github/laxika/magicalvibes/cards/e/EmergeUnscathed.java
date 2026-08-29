package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "20")
public class EmergeUnscathed extends Card {

    public EmergeUnscathed() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceUntilEndOfTurnEffect());
    }
}
