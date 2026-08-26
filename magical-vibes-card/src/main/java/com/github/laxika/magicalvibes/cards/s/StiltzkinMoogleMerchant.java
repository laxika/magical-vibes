package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FIN", collectorNumber = "34")
public class StiltzkinMoogleMerchant extends Card {

    public StiltzkinMoogleMerchant() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new TargetPlayerGainsControlOfTargetPermanentEffect(new DrawCardEffect(1))),
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
