package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.AllowCastTargetCardFromGraveyardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

/**
 * Jace, Telepath Unbound — back face of Jace, Vryn's Prodigy.
 * Legendary Planeswalker — Jace (Blue).
 */
public class JaceTelepathUnbound extends Card {

    private static final CardAnyOfPredicate INSTANT_OR_SORCERY = new CardAnyOfPredicate(List.of(
            new CardTypePredicate(CardType.INSTANT),
            new CardTypePredicate(CardType.SORCERY)));

    public JaceTelepathUnbound() {
        // +1: Up to one target creature gets -2/-0 until your next turn.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new BoostTargetCreatureEffect(-2, 0, GrantDuration.UNTIL_YOUR_NEXT_TURN)),
                "+1: Up to one target creature gets -2/-0 until your next turn.",
                null, +1, null, null,
                List.<TargetFilter>of(TargetFilters.creature()),
                0, 1
        ));

        // −3: You may cast target instant or sorcery card from your graveyard this turn. If that
        // spell would be put into your graveyard, exile it instead.
        // The permission lasts the whole turn (it is not a cast offered on resolution), so the card
        // stays in the graveyard until its controller chooses to cast it.
        addActivatedAbility(new ActivatedAbility(
                -3,
                List.of(new AllowCastTargetCardFromGraveyardThisTurnEffect(
                        INSTANT_OR_SORCERY, GraveyardSearchScope.CONTROLLERS_GRAVEYARD, true)),
                "-3: You may cast target instant or sorcery card from your graveyard this turn. "
                        + "If that spell would be put into your graveyard, exile it instead."
        ));

        // −9: You get an emblem with "Whenever you cast a spell, target opponent mills five cards."
        addActivatedAbility(new ActivatedAbility(
                -9,
                List.of(new CreateEmblemEffect(
                        List.of(new MillEffect(5, MillRecipient.TARGET_PLAYER)),
                        "Whenever you cast a spell, target opponent mills five cards.")),
                "-9: You get an emblem with \"Whenever you cast a spell, target opponent mills five cards.\""
        ));
    }
}
