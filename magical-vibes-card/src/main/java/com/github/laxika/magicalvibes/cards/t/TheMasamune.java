package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AdditionalCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;

@CardRegistration(set = "FIN", collectorNumber = "264")
public class TheMasamune extends Card {

    public TheMasamune() {
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.EQUIPPED_CREATURE),
                null));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsAttackingPredicate(),
                new MustBeBlockedIfAbleEffect(),
                null));
        addEffect(EffectSlot.STATIC, new AdditionalCreatureDeathTriggerEffect(
                new PermanentIsHostOfSourceAuraPredicate(), true));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
