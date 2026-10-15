package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.HighestOpponentLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "79")
public class RoilingHorror extends Card {

    public RoilingHorror() {
        Sum lifeDifference = new Sum(new ControllerLifeTotal(),
                new Scaled(new HighestOpponentLifeTotal(), -1));
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(lifeDifference, lifeDifference));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
                new GainLifeEffect(1));
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{X}{B}{B}{B}",
                List.of(),
                "Suspend X—{X}{B}{B}{B}",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withSuspendsSourceFromHandX());
    }
}
