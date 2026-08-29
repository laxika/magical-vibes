package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantColorEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.SetNameEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "FDN", collectorNumber = "168")
public class WitnessProtection extends Card {

    public WitnessProtection() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new SetNameEffect("Legitimate Businessperson", GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.CREATURE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantSubtypeEffect(CardSubtype.CITIZEN, GrantScope.ENCHANTED_CREATURE, true))
                .addEffect(EffectSlot.STATIC,
                        new BecomeColorlessEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantColorEffect(CardColor.GREEN, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantColorEffect(CardColor.WHITE, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
