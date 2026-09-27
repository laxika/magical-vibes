package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "59")
@CardRegistration(set = "FIC", collectorNumber = "155")
public class StragoAndRelm extends Card {

    public StragoAndRelm() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}{R}",
                List.of(new RevealTopCardsOfTargetPlayerUntilInstantOrSorceryAndCastEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY),
                                new CardTypePredicate(CardType.CREATURE))),
                        true,
                        true)),
                "Sketch and Lore — {2}{R}, {T}: Target opponent exiles cards from the top of their library "
                        + "until they exile an instant, sorcery, or creature card. You may cast that card "
                        + "without paying its mana cost. If you cast a creature spell this way, it gains haste "
                        + "and \"At the beginning of the end step, sacrifice this creature.\" Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "You must target an opponent."),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
