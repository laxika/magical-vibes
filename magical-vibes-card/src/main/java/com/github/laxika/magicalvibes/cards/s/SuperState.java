package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "SLD", collectorNumber = "2081")
public class SuperState extends Card {

    public SuperState() {
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(9, 9, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(
                                Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE, Keyword.TRAMPLE, Keyword.HASTE),
                                GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new EnchantedCreatureDealsDamageToEachOpponentEffect());
    }
}
