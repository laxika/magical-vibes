package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;

import java.util.List;

@CardRegistration(set = "ZEN", collectorNumber = "218")
public class MagosiTheWaterveil extends Card {

    public MagosiTheWaterveil() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLUE));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(new PutCountersOnSelfEffect(CounterType.EON), new SkipNextEffect(SkipKind.TURN)),
                "{U}, {T}: Put an eon counter on this land. Skip your next turn."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.EON),
                        ReturnToHandEffect.self(),
                        new ControllerExtraTurnEffect(1)
                ),
                "{T}, Remove an eon counter from this land and return it to its owner's hand: "
                        + "Take an extra turn after this one."
        ));
    }
}
