package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALA", collectorNumber = "65")
public class BanewaspAffliction extends Card {

    public BanewaspAffliction() {
        // Enchant creature. When enchanted creature dies, that creature's controller loses life equal
        // to its toughness. The amount is baked from the dying creature's toughness at trigger time.
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new EnchantedCreatureControllerLosesLifeEffect(0));
    }
}
