package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerHasCityBlessing;
import com.github.laxika.magicalvibes.model.effect.AscendEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PutSelfOnBottomOfOwnersLibraryCost;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "59")
public class TimestreamNavigator extends Card {

    public TimestreamNavigator() {
        addEffect(EffectSlot.STATIC, new AscendEffect());
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{U}{U}",
                List.of(new PutSelfOnBottomOfOwnersLibraryCost(), new ControllerExtraTurnEffect(1)),
                "{2}{U}{U}, {T}, Put this creature on the bottom of its owner's library: Take an extra turn after this one. Activate only if you have the city's blessing."
        ).withActivationCondition(new ControllerHasCityBlessing(),
                "Activate only if you have the city's blessing"));
    }
}
