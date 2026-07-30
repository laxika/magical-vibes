package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AttachedPermanentControllerControlsNoOther;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "117")
public class PredatorsGambit extends Card {

    public PredatorsGambit() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +2/+1.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.ENCHANTED_CREATURE))
                // Enchanted creature has intimidate as long as its controller controls no other creatures.
                .addEffect(EffectSlot.STATIC,
                        new ConditionalEffect(
                                new AttachedPermanentControllerControlsNoOther(new PermanentIsCreaturePredicate()),
                                new GrantKeywordEffect(Keyword.INTIMIDATE, GrantScope.ENCHANTED_CREATURE)));
    }
}
