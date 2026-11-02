package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "58")
public class MarkOfEviction extends Card {

    public MarkOfEviction() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.upkeep(ActivePlayer.YOU), ReturnToHandEffect.enchantedAndAuras());
    }
}
