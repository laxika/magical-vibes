package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersFromEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "55")
public class HonestWork extends Card {

    public HonestWork() {
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RemoveAllCountersFromEnchantedCreatureEffect())
                .addStaticEffect(new SetNameEffect("Humble Merchant", GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSubtypeEffect(CardSubtype.CITIZEN, GrantScope.ENCHANTED_CREATURE, true))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantActivatedAbilityEffect(
                                ManaAbilities.tapFor(ManaColor.COLORLESS),
                                GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
