package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AwardPersistentManaEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BOK", collectorNumber = "135")
public class MarkOfSakiko extends Card {

    public MarkOfSakiko() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new AwardPersistentManaEffect(ManaColor.GREEN, new EventValue(),
                                AwardPersistentManaEffect.Recipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
