package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LoseAllLandTypesEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ZNR", collectorNumber = "109")
public class LithoformBlight extends Card {

    public LithoformBlight() {
        target(TargetFilters.land());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addStaticEffect(new LoseAllLandTypesEffect(GrantScope.ENCHANTED_PERMANENT, null));
        addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_PERMANENT));
        addStaticEffect(new GrantActivatedAbilityEffect(
                ManaAbilities.tapFor(ManaColor.COLORLESS),
                GrantScope.ENCHANTED_PERMANENT));
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        true,
                        null,
                        List.of(new PayLifeCost(1), new AwardAnyColorManaEffect()),
                        "{T}, Pay 1 life: Add one mana of any color."),
                GrantScope.ENCHANTED_PERMANENT));
    }
}
