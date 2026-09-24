package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentBecomesTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetCardTypesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "47")
public class SongOfTheDryads extends Card {

    public SongOfTheDryads() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.STATIC,
                        new SetCardTypesEffect(Set.of(CardType.LAND), GrantScope.ENCHANTED_PERMANENT))
                .addEffect(EffectSlot.STATIC,
                        new EnchantedPermanentBecomesTypeEffect(CardSubtype.FOREST))
                .addEffect(EffectSlot.STATIC,
                        new BecomeColorlessEffect(GrantScope.ENCHANTED_PERMANENT));
    }
}
