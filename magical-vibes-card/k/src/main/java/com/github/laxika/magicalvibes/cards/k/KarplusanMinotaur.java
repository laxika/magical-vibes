package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "86")
public class KarplusanMinotaur extends Card {

    public KarplusanMinotaur() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), CumulativeUpkeepEffect.flipCoin());
        addEffect(EffectSlot.ON_CONTROLLER_WINS_COIN_FLIP, new DealDamageToAnyTargetEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_LOSES_COIN_FLIP, new DealDamageToAnyTargetEffect(1));
    }
}
