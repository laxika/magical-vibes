package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Objects;

/**
 * "You may play that card [this turn | until the end of your next turn | for as long as it remains exiled | for as long
 * as you control this creature]": each card {@link #cards()} names that is in exile gets a play permission for the
 * effect's controller lasting {@link #duration()}; a card the subject finds in another zone gets none, and every
 * permission ends as the card leaves exile (CR 400.7). It follows the step that exiled the cards, over
 * {@code Subjects.cardsThisWay(StepKind.EXILE)} ("exile target card from your graveyard. You may play it this turn") or
 * {@code cardsThisWay(StepKind.COUNTER_SPELL)} (Spelljack's "exile it instead … You may play it without paying its mana
 * cost for as long as it remains exiled").
 *
 * <p>{@link Duration#WHILE_SOURCE_CONTROLLED} ("for as long as you control Victor Mancha") is granted only while the
 * effect's controller controls the source permanent as the effect applies; it ends when they stop.
 *
 * @param cards                  the cards that may be played
 * @param duration               how long the permission lasts
 * @param freeCast               the cards are played without paying their mana costs
 * @param anyManaType            mana of any type can be spent to cast a card this way (marked on a land card too: it
 *                               may have a spell to cast, an Adventure)
 * @param exileInsteadOfGraveyard "if that spell would be put into a graveyard, exile it instead"
 * @param playCondition          "during any turn you attacked with a Wolf or Werewolf, you may cast that card" (Rahilda),
 *                               "you may play cards exiled with this enchantment as long as you attacked this turn"
 *                               (Pick Up the Pace): the permission is usable only while it holds, judged as the card is
 *                               played; {@code null} for always
 * @param ownerPlays             "its owner may play it" (Suspend Aggression, Release to the Wind): each card's owner holds
 *                               its permission instead of the effect's controller, "their next turn" being the owner's
 */
public record AllowPlayCardsFromExileEffect(CardSubject cards, Duration duration, boolean freeCast,
                                            boolean anyManaType, boolean exileInsteadOfGraveyard,
                                            Condition playCondition, boolean ownerPlays)
        implements CardEffect {

    public AllowPlayCardsFromExileEffect {
        Objects.requireNonNull(cards, "cards");
        Objects.requireNonNull(duration, "duration");
        if (cards.declaresTarget()) {
            throw new IllegalArgumentException("The permission names the cards an earlier step moved: " + cards);
        }
        if (ownerPlays && Duration.WHILE_SOURCE_CONTROLLED.equals(duration)) {
            throw new IllegalArgumentException("A permission held while you control the source is yours");
        }
    }

    public AllowPlayCardsFromExileEffect(CardSubject cards, Duration duration, boolean freeCast, boolean anyManaType,
                                         boolean exileInsteadOfGraveyard) {
        this(cards, duration, freeCast, anyManaType, exileInsteadOfGraveyard, null, false);
    }

    /** "You may play that card for as long as it remains exiled." */
    public static AllowPlayCardsFromExileEffect whileExiled(CardSubject cards) {
        return new AllowPlayCardsFromExileEffect(cards, Duration.PERMANENT, false, false, false);
    }

    /** "You may play / cast that card this turn." */
    public static AllowPlayCardsFromExileEffect thisTurn(CardSubject cards) {
        return new AllowPlayCardsFromExileEffect(cards, Duration.UNTIL_END_OF_TURN, false, false, false);
    }

    /** "Until the end of your next turn, you may play that card." */
    public static AllowPlayCardsFromExileEffect untilEndOfYourNextTurn(CardSubject cards) {
        return new AllowPlayCardsFromExileEffect(cards, Duration.UNTIL_END_OF_YOUR_NEXT_TURN, false, false, false);
    }

    /** "You may play it for as long as you control this creature." */
    public static AllowPlayCardsFromExileEffect whileYouControlSource(CardSubject cards) {
        return new AllowPlayCardsFromExileEffect(cards, Duration.WHILE_SOURCE_CONTROLLED, false, false, false);
    }

    /** "… without paying its mana cost." */
    public AllowPlayCardsFromExileEffect withoutPayingManaCost() {
        return new AllowPlayCardsFromExileEffect(cards, duration, true, anyManaType, exileInsteadOfGraveyard,
                playCondition, ownerPlays);
    }

    /** "… and mana of any type can be spent to cast that spell." */
    public AllowPlayCardsFromExileEffect withAnyManaType() {
        return new AllowPlayCardsFromExileEffect(cards, duration, freeCast, true, exileInsteadOfGraveyard,
                playCondition, ownerPlays);
    }

    /** "If that spell would be put into a graveyard, exile it instead." */
    public AllowPlayCardsFromExileEffect exilingInsteadOfGraveyard() {
        return new AllowPlayCardsFromExileEffect(cards, duration, freeCast, anyManaType, true, playCondition,
                ownerPlays);
    }

    /** "During any turn [condition], you may cast that card", "… as long as [condition]". */
    public AllowPlayCardsFromExileEffect playableOnlyIf(Condition condition) {
        return new AllowPlayCardsFromExileEffect(cards, duration, freeCast, anyManaType, exileInsteadOfGraveyard,
                Objects.requireNonNull(condition, "condition"), ownerPlays);
    }

    /** "Its owner may play it": each card's owner, not you, may play it. */
    public AllowPlayCardsFromExileEffect playableByOwner() {
        return new AllowPlayCardsFromExileEffect(cards, duration, freeCast, anyManaType, exileInsteadOfGraveyard,
                playCondition, true);
    }
}
