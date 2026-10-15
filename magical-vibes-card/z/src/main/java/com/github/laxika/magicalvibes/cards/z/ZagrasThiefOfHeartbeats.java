package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;

@CardRegistration(set = "ZNR", collectorNumber = "241")
public class ZagrasThiefOfHeartbeats extends Card {

    public ZagrasThiefOfHeartbeats() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new PartySize()));
        addEffect(EffectSlot.STATIC, new GrantKeywordEffect(Keyword.DEATHTOUCH, GrantScope.OWN_CREATURES));
        addEffect(EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER,
                TriggeringPermanentConditionalEffect.combatDamageOnly(new DestroyTargetPermanentEffect()));
    }
}
