package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToEachOpponentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsHostOfSourceAuraPredicate;

@CardRegistration(set = "MSH", collectorNumber = "132")
public class HawkeyesBow extends Card {

    public HawkeyesBow() {
        addStaticEffect(new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new GrantKeywordEffect(Keyword.REACH, GrantScope.EQUIPPED_CREATURE));

        TriggeringPermanentConditionalEffect damageTrigger = new TriggeringPermanentConditionalEffect(
                new PermanentIsHostOfSourceAuraPredicate(),
                new EnchantedCreatureDealsDamageToEachOpponentEffect(new Fixed(1)));
        addEffect(EffectSlot.ON_ALLY_PERMANENT_BECOMES_TAPPED, damageTrigger);
        addEffect(EffectSlot.ON_OPPONENT_PERMANENT_BECOMES_TAPPED, damageTrigger);

        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
