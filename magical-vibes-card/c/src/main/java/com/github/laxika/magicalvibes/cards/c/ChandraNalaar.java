package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "159")
@CardRegistration(set = "M10", collectorNumber = "132")
@CardRegistration(set = "M11", collectorNumber = "127")
@CardRegistration(set = "DD2", collectorNumber = "34")
@CardRegistration(set = "JVC", collectorNumber = "34")
@CardRegistration(set = "SLD", collectorNumber = "748")
@CardRegistration(set = "SLD", collectorNumber = "1456")
public class ChandraNalaar extends Card {

    public ChandraNalaar() {
        // +1: Chandra Nalaar deals 1 damage to target player or planeswalker.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(DealDamageEffect.toAnyTarget(1)),
                "+1: Chandra Nalaar deals 1 damage to target player or planeswalker.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                )
        ));

        // −X: Chandra Nalaar deals X damage to target creature.
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(DealDamageEffect.toTargetCreature(new XValue())),
                "\u2212X: Chandra Nalaar deals X damage to target creature.",
                null
        ));

        // −8: Chandra Nalaar deals 10 damage to target player or planeswalker
        // and each creature that player or that planeswalker's controller controls.
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(SequenceEffect.simultaneous(
                        DealDamageEffect.toTargetPlayerOrPlaneswalker(10),
                        DealDamageEffect.toCreaturesOfTargetedPlayer(new Fixed(10)))),
                "\u22128: Chandra Nalaar deals 10 damage to target player or planeswalker and each creature that player or that planeswalker's controller controls.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        "Target must be a player or planeswalker"
                )
        ));
    }
}
