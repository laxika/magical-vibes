package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "2")
public class BatheInLight extends Card {

    public BatheInLight() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new GrantProtectionChoiceToTargetAndSharingCreaturesUntilEndOfTurnEffect());
    }
}
