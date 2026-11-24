package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "You may cast it without paying its mana cost", as the ability resolves: each card {@link #cards()} names that
 * matches {@link #filter()} is offered for a free cast where it is: in exile, whoever owns it, or in the caster's
 * own graveyard. Transcendent Dragon's "exile it instead …, then you may cast it" is
 * {@code Subjects.cardsThisWay(COUNTER_SPELL)} after {@code CounterSpellEffect(EXILE)}. Malcolm, Alluring
 * Scoundrel's "you may cast the discarded card" is {@code Subjects.cardsThisWay(DISCARD)};
 * Hidetsugu and Kairi's "if it's an instant or sorcery card, you may cast it" after its exile step is
 * {@code Subjects.cardsThisWay(EXILE)} with an instant-or-sorcery filter. A declined card stays where it is.
 *
 * <p>The filter is bound as the step resolves ({@code SubjectResolutionService.bindResolutionFilter}), so "that card"
 * after an exile-until step is {@code cardsThisWay(EXILE)} with the step's own stop: "a card that shares a card type
 * with it" (Possibility Storm), "a nonland card with a different name than that spell" (Tibalt's Trickery).
 * {@link #castBy} is "that player may cast that card": the caster is offered the card and casts it (CR 608.2g).
 *
 * @param cards  the cards offered
 * @param filter which of them may be cast; {@code null} for any nonland card (a land card can't be cast)
 * @param caster who may cast them; {@code null} for the controller
 */
public record MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards, CardPredicate filter,
                                                              PlayerSubject caster)
        implements CardEffect {

    public MayCastDiscardedCardWithoutPayingManaCostEffect {
        Objects.requireNonNull(cards, "cards");
        if (caster != null && caster.declaresTarget()) {
            throw new IllegalArgumentException("The caster is named without a target: " + caster);
        }
    }

    /** "You may cast [those of them that match] without paying their mana cost". */
    public MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards, CardPredicate filter) {
        this(cards, filter, null);
    }

    /** "You may cast [those cards] without paying their mana cost": any nonland card {@code cards} names. */
    public MayCastDiscardedCardWithoutPayingManaCostEffect(CardSubject cards) {
        this(cards, null);
    }

    /** "That player / they may cast that card": the first player {@code caster} names is offered the cast. */
    public MayCastDiscardedCardWithoutPayingManaCostEffect castBy(PlayerSubject caster) {
        return new MayCastDiscardedCardWithoutPayingManaCostEffect(cards, filter,
                Objects.requireNonNull(caster, "caster"));
    }
}
