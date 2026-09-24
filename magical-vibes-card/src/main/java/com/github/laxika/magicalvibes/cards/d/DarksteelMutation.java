package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "142")
public class DarksteelMutation extends Card {

    public DarksteelMutation() {
        target(TargetFilters.creature())
                // Enchanted creature is an artifact creature
                .addEffect(EffectSlot.STATIC, new SetCardTypesEffect(
                        Set.of(CardType.ARTIFACT, CardType.CREATURE), GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has base power and toughness 0/1
                .addEffect(EffectSlot.STATIC, new SetBasePowerToughnessEffect(
                        0, 1, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature is an Insect, replacing its other creature types
                .addEffect(EffectSlot.STATIC, new GrantSubtypeEffect(
                        CardSubtype.INSECT, GrantScope.ENCHANTED_CREATURE, true))
                // Enchanted creature loses all other abilities
                .addEffect(EffectSlot.STATIC, new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GrantKeywordEffect(
                        Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_CREATURE));
    }
}
