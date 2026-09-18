package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.Set;

@CardRegistration(set = "DMU", collectorNumber = "231")
public class HerosHeirloom extends Card {

    public HerosHeirloom() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                new GrantKeywordEffect(Set.of(Keyword.TRAMPLE, Keyword.HASTE), GrantScope.EQUIPPED_CREATURE),
                null));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
