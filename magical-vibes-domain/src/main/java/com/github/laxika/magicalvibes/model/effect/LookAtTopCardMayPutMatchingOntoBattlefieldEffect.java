package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Look at the top card of the controller's library. If it matches {@code predicate}, the controller
 * may put that card onto the battlefield (tapped with {@code enterTapped}). A card not put there is
 * handled as {@code otherwise} says: it stays on top, it is put into the controller's hand, or the
 * controller may put it on the bottom of their library.
 *
 * <p>Used by Into the Wilds with {@code CardTypePredicate(LAND)}; Fecund Greenshell and Believe use
 * {@link Otherwise#HAND}; Lantern of Revealing {@link Otherwise#MAY_BOTTOM}. The predicate is judged with the
 * source permanent, else its last-known snapshot ("mana value less than or equal to the number of loyalty
 * counters on Nissa", {@code CardManaValueAtMostSourceCountersPredicate}, CR 608.2h).
 *
 * <p>A {@code StepKind.RETURN} step: nothing as it begins, the permanent the card became once the put is
 * accepted. "If you didn't put a card onto the battlefield this way" (Rulik Mons) is
 * {@code ConditionalEffect(NotCondition(HappenedThisWay.returned()), …)} after it. The {@link Stage} field
 * distinguishes the look (stack resolution) from the may-ability follow-ups.
 */
public record LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate,
                                                                 boolean enterTapped,
                                                                 Otherwise otherwise,
                                                                 Stage stage)
        implements CardEffect {

    /** Where a card not put onto the battlefield goes. */
    public enum Otherwise {
        /** It stays on top of the library. */
        TOP,
        /** It is put into the controller's hand. */
        HAND,
        /** The controller may put it on the bottom of their library. */
        MAY_BOTTOM
    }

    public enum Stage {
        /** Stack resolution: private look, then the may to put it onto the battlefield. */
        LOOK,
        /** May: put the matching top card onto the battlefield. */
        MAY_PUT,
        /** May: put the top card on the bottom of the library. */
        MAY_BOTTOM
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect {
        Objects.requireNonNull(otherwise, "otherwise");
        Objects.requireNonNull(stage, "stage");
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate, boolean enterTapped,
                                                            Otherwise otherwise) {
        this(predicate, enterTapped, otherwise, Stage.LOOK);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate, boolean enterTapped,
                                                            boolean otherwiseToHand) {
        this(predicate, enterTapped, otherwiseToHand ? Otherwise.HAND : Otherwise.TOP);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate) {
        this(predicate, false, Otherwise.TOP);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect(CardPredicate predicate, boolean enterTapped) {
        this(predicate, enterTapped, Otherwise.TOP);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldEffect withStage(Stage stage) {
        return new LookAtTopCardMayPutMatchingOntoBattlefieldEffect(predicate, enterTapped, otherwise, stage);
    }
}
