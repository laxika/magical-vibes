package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesOfSubtypeThisTurn;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "246")
public class ThaumatonTorpedo extends Card {

    public ThaumatonTorpedo() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}",
                List.of(
                        new ReduceActivationCostEffect(new FixedIfCondition(
                                new AttackedWithCreaturesOfSubtypeThisTurn(1, CardSubtype.SPACECRAFT), 3, 0)),
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect()),
                "{6}, {T}, Sacrifice this artifact: Destroy target nonland permanent. "
                        + "This ability costs {3} less to activate if you attacked with a Spacecraft this turn.",
                TargetFilters.nonlandPermanent()));
    }
}
