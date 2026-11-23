package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "34")
@CardRegistration(set = "FIN", collectorNumber = "327")
@CardRegistration(set = "FIN", collectorNumber = "433")
@CardRegistration(set = "FIN", collectorNumber = "565")
public class StiltzkinMoogleMerchant extends Card {

    public StiltzkinMoogleMerchant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new GainControlOfTargetEffect(Duration.PERMANENT).gainedBy(Subjects.targetPlayer(0)),
                        new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL), new DrawCardEffect(1))
                ),
                "{2}, {T}: Target opponent gains control of another target permanent you control. If they do, you draw a card.",
                List.of(
                        new PlayerPredicateTargetFilter(
                                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                                "Target must be an opponent"),
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate()),
                                "Target must be another permanent you control")
                ),
                2,
                2
        ));
    }
}
