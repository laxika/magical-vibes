package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "70")
public class TamiyosLogbook extends Card {

    public TamiyosLogbook() {
        var otherArtifacts = new PermanentCount(
                new PermanentIsArtifactPredicate(), CountScope.CONTROLLER, true);

        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{U}",
                List.of(
                        new ReduceActivationCostEffect(otherArtifacts),
                        new DrawCardEffect(1)
                ),
                "{5}{U}, {T}: Draw a card. This ability costs {1} less to activate for each other artifact you control."
        ));
    }
}
