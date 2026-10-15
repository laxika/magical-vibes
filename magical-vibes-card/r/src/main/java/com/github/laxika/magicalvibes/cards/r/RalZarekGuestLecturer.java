package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.EachTargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinsPerHeadsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SkipKind;
import com.github.laxika.magicalvibes.model.effect.SkipNextEffect;
import com.github.laxika.magicalvibes.model.effect.SkipRecipient;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "SOS", collectorNumber = "97")
public class RalZarekGuestLecturer extends Card {

    public RalZarekGuestLecturer() {
        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new SurveilEffect(2)),
                "+1: Surveil 2."
        ));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new EachTargetPlayerDiscardsEffect(1)),
                "−1: Any number of target players each discard a card.",
                anyPlayer(), -1, null, null, List.of(), 0, 99
        ));

        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardMaxManaValuePredicate(3))))
                        .targetGraveyard(true)
                        .build()),
                "−2: Return target creature card with mana value 3 or less from your graveyard to the battlefield."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new FlipCoinsPerHeadsEffect(5,
                        new SkipNextEffect(SkipKind.TURN, SkipRecipient.TARGET_PLAYER))),
                "−7: Flip five coins. Target opponent skips their next X turns, where X is the number of coins that came up heads.",
                opponent()
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }

    private static PlayerPredicateTargetFilter opponent() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        );
    }
}
