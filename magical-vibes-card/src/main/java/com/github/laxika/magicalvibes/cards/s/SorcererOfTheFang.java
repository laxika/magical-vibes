package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "M20", collectorNumber = "114")
public class SorcererOfTheFang extends Card {

    public SorcererOfTheFang() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}{B}",
                List.of(new DealDamageToTargetPlayerOrPlaneswalkerEffect(2, PlayerRelation.OPPONENT)),
                "{5}{B}, {T}: This creature deals 2 damage to target opponent or planeswalker.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or planeswalker")));
    }
}
