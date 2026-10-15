package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "36")
public class RitualOfSteel extends Card {

    public RitualOfSteel() {
        // Enchant creature — enchanted creature gets +0/+2.
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE));

        // When this Aura enters, draw a card at the beginning of the next turn's upkeep.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
