package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentExilesFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "140")
public class NicolBolasGodPharaoh extends Card {

    public NicolBolasGodPharaoh() {
        // +2: Target opponent exiles cards from the top of their library until they exile a
        // nonland card. Until end of turn, you may cast that card without paying its mana cost.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect()),
                "+2: Target opponent exiles cards from the top of their library until they exile a "
                        + "nonland card. Until end of turn, you may cast that card without paying its mana cost.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Must target an opponent"
                )
        ));

        // +1: Each opponent exiles two cards from their hand.
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new EachOpponentExilesFromHandEffect(2)),
                "+1: Each opponent exiles two cards from their hand."
        ));

        // −4: deals 7 damage to target opponent, creature an opponent controls, or planeswalker
        // an opponent controls. ANY_TARGET + filters restrict to opponents / their permanents.
        addActivatedAbility(new ActivatedAbility(
                -4,
                List.of(new DealDamageToAnyTargetEffect(7)),
                "−4: Nicol Bolas, God-Pharaoh deals 7 damage to target opponent, creature an "
                        + "opponent controls, or planeswalker an opponent controls.",
                new AnyTargetPredicateTargetFilter(
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent or a permanent an opponent controls"
                )
        ));

        // −12: Exile each nonland permanent your opponents control.
        addActivatedAbility(new ActivatedAbility(
                -12,
                List.of(new ExileAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                )))),
                "−12: Exile each nonland permanent your opponents control."
        ));
    }
}
