package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.condition.HasAttacker;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "WOE", collectorNumber = "318")
public class ChargingHooligan extends Card {

    public ChargingHooligan() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.ANY_PLAYER, false, true),
                new Fixed(0)));
        addEffect(EffectSlot.ON_ATTACK, ConditionalEffect.unless(
                new HasAttacker(new PermanentHasSubtypePredicate(CardSubtype.RAT)),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
