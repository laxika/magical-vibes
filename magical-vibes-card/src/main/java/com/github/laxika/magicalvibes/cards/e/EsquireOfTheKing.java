package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;

import java.util.List;

@CardRegistration(set = "HOC", collectorNumber = "166")
public class EsquireOfTheKing extends Card {

    public EsquireOfTheKing() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}{W}",
                List.of(
                        new ReduceActivationCostEffect(new FixedIfCondition(
                                new ControlsPermanent(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)),
                                2,
                                0)),
                        new BoostAllOwnCreaturesEffect(1, 1)),
                "{4}{W}, {T}: Creatures you control get +1/+1 until end of turn. "
                        + "This ability costs {2} less to activate if you control a legendary creature."
        ));
    }
}
