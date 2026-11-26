package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "175")
@CardRegistration(set = "C15", collectorNumber = "296")
@CardRegistration(set = "C21", collectorNumber = "305")
public class NovijenHeartOfProgress extends Card {

    public NovijenHeartOfProgress() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.COLORLESS));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}{U}",
                List.of(PutCountersEffect.onEach(CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentEnteredBattlefieldThisTurnPredicate()
                        )))),
                "{G}{U}, {T}: Put a +1/+1 counter on each creature that entered the battlefield this turn."
        ));
    }
}
