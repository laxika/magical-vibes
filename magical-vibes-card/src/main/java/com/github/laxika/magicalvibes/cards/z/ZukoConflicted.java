package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeNotYetChosenEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnUnderOpponentControlEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "253")
public class ZukoConflicted extends Card {

    public ZukoConflicted() {
        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED, new ChooseModeNotYetChosenEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Draw a card", new DrawCardEffect()),
                new ChooseOneEffect.ChooseOneOption("Put a +1/+1 counter on Zuko",
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                new ChooseOneEffect.ChooseOneOption("Add {R}", new AwardManaEffect(ManaColor.RED)),
                new ChooseOneEffect.ChooseOneOption(
                        "Exile Zuko, then return him to the battlefield under an opponent's control",
                        new ExileSelfAndReturnUnderOpponentControlEffect()))));
    }
}
