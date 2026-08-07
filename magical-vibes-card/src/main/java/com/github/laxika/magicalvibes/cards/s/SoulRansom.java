package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "198")
public class SoulRansom extends Card {

    public SoulRansom() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new ControlEnchantedCreatureEffect());
        addActivatedAbility(new ActivatedAbility(false, null,
                        List.of(new DiscardCardTypeCost(null, null, 2),
                                new SacrificeSelfAndControllerDrawsEffect(2)),
                        "Discard two cards: This Aura's controller sacrifices it, then draws two cards. "
                                + "Only your opponents may activate this ability.")
                        .withActivatableByAnyPlayer()
                        .withActivatableOnlyByOpponents());
    }
}
