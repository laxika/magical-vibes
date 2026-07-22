package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "168")
public class RuneboundWolf extends Card {

    public RuneboundWolf() {
        // {3}{R}, {T}: This creature deals damage equal to the number of Wolves and
        // Werewolves you control to target opponent.
        PermanentCount wolvesAndWerewolves = new PermanentCount(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentHasSubtypePredicate(CardSubtype.WOLF),
                        new PermanentHasSubtypePredicate(CardSubtype.WEREWOLF))),
                CountScope.CONTROLLER);
        addActivatedAbility(new ActivatedAbility(
                true, "{3}{R}",
                List.of(new DealDamageToPlayersEffect(
                        wolvesAndWerewolves, DamageRecipient.TARGET_PLAYER)),
                "{3}{R}, {T}: This creature deals damage equal to the number of Wolves and "
                        + "Werewolves you control to target opponent.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent")));
    }
}
