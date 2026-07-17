package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinWinEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "254")
public class OrcishCaptain extends Card {

    public OrcishCaptain() {
        addActivatedAbility(new ActivatedAbility(false, "{1}",
                List.of(new FlipCoinWinEffect(
                        new BoostTargetCreatureEffect(2, 0),
                        new BoostTargetCreatureEffect(0, -2))),
                "{1}: Flip a coin. If you win the flip, target Orc creature gets +2/+0 until end of turn. "
                        + "If you lose the flip, it gets -0/-2 until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.ORC)
                        )),
                        "Target must be an Orc creature"
                )));
    }
}
