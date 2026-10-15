package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AnyGraveyardAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "57")
public class JaceThePerfectedMind extends Card {

    public JaceThePerfectedMind() {
        // +1: Until your next turn, up to one target creature gets -3/-0.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(-3, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Until your next turn, up to one target creature gets -3/-0.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()),
                0, 1
        ));

        // −2: Target player mills three cards. Then if a graveyard has twenty or more cards in it,
        // you draw three cards. Otherwise, you draw a card.
        AnyGraveyardAtLeast twentyCards = new AnyGraveyardAtLeast(20);
        addActivatedAbility(new ActivatedAbility(
                -2,
                List.of(
                        new MillEffect(3, MillRecipient.TARGET_PLAYER),
                        new ConditionalEffect(twentyCards, new DrawCardEffect(3)),
                        new ConditionalEffect(new NotCondition(twentyCards), new DrawCardEffect(1))
                ),
                "−2: Target player mills three cards. Then if a graveyard has twenty or more cards in it, "
                        + "you draw three cards. Otherwise, you draw a card.",
                anyPlayer()
        ));

        // −X: Target player mills three times X cards.
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(new MillEffect(new Scaled(new XValue(), 3), MillRecipient.TARGET_PLAYER)),
                "−X: Target player mills three times X cards.",
                anyPlayer()
        ));
    }

    private static PlayerPredicateTargetFilter anyPlayer() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
    }
}
