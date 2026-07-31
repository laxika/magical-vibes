package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "207")
public class ZealotOfTheGodPharaoh extends Card {

    public ZealotOfTheGodPharaoh() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{R}",
                List.of(new DealDamageToTargetOpponentOrPlaneswalkerEffect(2)),
                "{4}{R}: This creature deals 2 damage to target opponent or planeswalker.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or planeswalker")));
    }
}
