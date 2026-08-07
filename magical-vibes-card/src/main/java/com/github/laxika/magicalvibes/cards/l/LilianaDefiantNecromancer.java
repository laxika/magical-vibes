package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.CreateEmblemEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnDyingCreatureUnderControlEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/**
 * Liliana, Defiant Necromancer — back face of Liliana, Heretical Healer.
 * Legendary Planeswalker — Liliana (Black).
 */
public class LilianaDefiantNecromancer extends Card {

    public LilianaDefiantNecromancer() {
        // +2: Each player discards a card.
        addActivatedAbility(new ActivatedAbility(
                +2,
                List.of(new DiscardEffect(1, DiscardRecipient.EACH_PLAYER, false)),
                "+2: Each player discards a card."
        ));

        // −X: Return target nonlegendary creature card with mana value X from your graveyard to the
        // battlefield. X is the loyalty paid, and it also narrows the legal graveyard targets.
        addActivatedAbility(ActivatedAbility.variableLoyaltyAbility(
                List.of(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardNotPredicate(new CardSupertypePredicate(CardSupertype.LEGENDARY)))))
                        .targetGraveyard(true)
                        .requiresManaValueEqualsX(true)
                        .build()),
                "-X: Return target nonlegendary creature card with mana value X from your graveyard "
                        + "to the battlefield.",
                null
        ));

        // −8: You get an emblem with "Whenever a creature dies, return it to the battlefield under
        // your control at the beginning of the next end step."
        addActivatedAbility(new ActivatedAbility(
                -8,
                List.of(new CreateEmblemEffect(
                        List.of(new RegisterDelayedReturnDyingCreatureUnderControlEffect(
                                false, null, 0, null, null)),
                        "Whenever a creature dies, return it to the battlefield under your control at "
                                + "the beginning of the next end step.")),
                "-8: You get an emblem with \"Whenever a creature dies, return it to the battlefield "
                        + "under your control at the beginning of the next end step.\""
        ));
    }
}
