package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "86")
public class KarplusanMinotaur extends Card {

    public KarplusanMinotaur() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.flipCoin());
        addTrigger(Triggers.youWinCoinFlip(), new DealDamageToAnyTargetEffect(1));
        addTrigger(Triggers.youLoseCoinFlip(), DealDamageToAnyTargetEffect.toAnyTargetOfAnOpponentsChoice(1));
    }
}
