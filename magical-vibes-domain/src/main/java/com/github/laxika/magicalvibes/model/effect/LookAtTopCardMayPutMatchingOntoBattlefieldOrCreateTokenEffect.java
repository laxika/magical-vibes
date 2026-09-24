package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Looks at the top card of the controller's library and may put it onto the battlefield when it
 * matches the predicate; when it is not put onto the battlefield, resolves the fallback token
 * creation effect instead.
 */
public record LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect(
        CardPredicate predicate,
        boolean enterTapped,
        CreateTokenEffect fallbackToken,
        Stage stage
) implements CardEffect {

    public LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect(
            CardPredicate predicate, boolean enterTapped, CreateTokenEffect fallbackToken) {
        this(predicate, enterTapped, fallbackToken, Stage.LOOK);
    }

    public LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect withStage(Stage stage) {
        return new LookAtTopCardMayPutMatchingOntoBattlefieldOrCreateTokenEffect(
                predicate, enterTapped, fallbackToken, stage);
    }

    public enum Stage {
        LOOK,
        MAY_PUT
    }
}
