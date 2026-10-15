package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentsSacrificedThisTurn;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

import java.util.List;

@CardRegistration(set = "SNC", collectorNumber = "118")
public class PyreSledgeArsonist extends Card {

    public PyreSledgeArsonist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new DealDamageToAnyTargetEffect(
                        new PermanentsSacrificedThisTurn(CountScope.CONTROLLER))),
                "{1}, {T}: This creature deals X damage to any target, where X is the number of permanents you've sacrificed this turn."
        ));
    }
}
