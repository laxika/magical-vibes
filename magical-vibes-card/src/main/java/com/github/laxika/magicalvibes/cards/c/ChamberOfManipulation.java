package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "76")
public class ChamberOfManipulation extends Card {

    public ChamberOfManipulation() {
        // Enchant land — grants "{T}, Discard a card: Gain control of target creature until end of turn."
        target(TargetFilters.land())
                .addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                        new ActivatedAbility(
                                true,
                                null,
                                List.of(
                                        new DiscardCardTypeCost(null, null),
                                        new GainControlOfTargetEffect(ControlDuration.END_OF_TURN)
                                ),
                                "{T}, Discard a card: Gain control of target creature until end of turn.",
                                TargetFilters.creature()
                        ),
                        GrantScope.ENCHANTED_PERMANENT
                ));
    }
}
