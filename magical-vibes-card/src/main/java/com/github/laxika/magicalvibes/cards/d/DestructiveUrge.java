package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "USG", collectorNumber = "180")
public class DestructiveUrge extends Card {

    public DestructiveUrge() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new SacrificePermanentsEffect(1, new PermanentIsLandPredicate(),
                                SacrificeRecipient.TARGET_PLAYER));
    }
}
