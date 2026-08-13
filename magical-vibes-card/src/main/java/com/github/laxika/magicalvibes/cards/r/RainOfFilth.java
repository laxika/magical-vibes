package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "151")
public class RainOfFilth extends Card {

    public RainOfFilth() {
        ActivatedAbility sacrificeLandForBlackMana = new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new AwardManaEffect(ManaColor.BLACK)),
                "Sacrifice this land: Add {B}."
        );
        addEffect(EffectSlot.SPELL, new GrantActivatedAbilityEffect(
                sacrificeLandForBlackMana,
                GrantScope.OWN_LANDS,
                null,
                EffectDuration.UNTIL_END_OF_TURN
        ));
    }
}
