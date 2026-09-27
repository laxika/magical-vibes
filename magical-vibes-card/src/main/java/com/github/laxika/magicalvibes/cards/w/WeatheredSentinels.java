package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.CanAttackPlayersWhoAttackedControllerLastTurnAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "NCC", collectorNumber = "85")
@CardRegistration(set = "NCC", collectorNumber = "185")
public class WeatheredSentinels extends Card {

    public WeatheredSentinels() {
        addEffect(EffectSlot.STATIC,
                new CanAttackPlayersWhoAttackedControllerLastTurnAsThoughNoDefenderEffect());
        addEffect(EffectSlot.ON_ATTACK, SequenceEffect.of(
                new BoostSelfEffect(3, 3),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
    }
}
