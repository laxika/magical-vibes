package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackPlayersWhoAttackedControllerLastTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "TDC", collectorNumber = "336")
public class WeatheredSentinels extends Card {

    public WeatheredSentinels() {
        addEffect(EffectSlot.STATIC, new CanAttackPlayersWhoAttackedControllerLastTurnEffect());
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(3, 3));
        addEffect(EffectSlot.ON_ATTACK, new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF));
    }
}
