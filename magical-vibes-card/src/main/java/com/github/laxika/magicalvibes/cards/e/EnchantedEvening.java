package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SHM", collectorNumber = "140")
public class EnchantedEvening extends Card {

    public EnchantedEvening() {
        // All permanents are enchantments in addition to their other types.
        addEffect(EffectSlot.STATIC, new GrantCardTypeEffect(CardType.ENCHANTMENT, GrantScope.ALL_PERMANENTS));
    }
}
