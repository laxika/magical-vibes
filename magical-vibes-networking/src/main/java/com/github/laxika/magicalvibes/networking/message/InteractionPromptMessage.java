package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.model.InteractionShape;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

/**
 * The single server-to-client prompt for every pending interaction. The {@link InteractionShape}
 * tells the client which input UI to show and which {@link InteractionAnswerRequest} payload to
 * answer with; the remaining fields carry the shape's data and presentation extras (unused
 * fields are null). Built via the per-shape static factories.
 */
public record InteractionPromptMessage(
        MessageType type,
        InteractionShape shape,
        String prompt,
        List<Integer> cardIndices,
        List<CardView> cards,
        List<UUID> cardIds,
        List<UUID> permanentIds,
        List<UUID> playerIds,
        List<String> options,
        Integer maxCount,
        Boolean declinable,
        Boolean canPay,
        String manaCost,
        String cardName,
        Boolean allGraveyards,
        Boolean searchable) {

    private static InteractionPromptMessage of(InteractionShape shape, String prompt,
                                               List<Integer> cardIndices, List<CardView> cards,
                                               List<UUID> cardIds, List<UUID> permanentIds,
                                               List<UUID> playerIds, List<String> options,
                                               Integer maxCount, Boolean declinable, Boolean canPay,
                                               String manaCost, String cardName,
                                               Boolean allGraveyards, Boolean searchable) {
        return new InteractionPromptMessage(MessageType.INTERACTION_PROMPT, shape, prompt,
                cardIndices, cards, cardIds, permanentIds, playerIds, options, maxCount,
                declinable, canPay, manaCost, cardName, allGraveyards, searchable);
    }

    /** Pick one card from the player's own hand by index. */
    public static InteractionPromptMessage cardIndexPick(List<Integer> cardIndices, String prompt,
                                                         boolean declinable) {
        return of(InteractionShape.CARD_INDEX_PICK, prompt, cardIndices, null, null, null, null,
                null, null, declinable, null, null, null, null, null);
    }

    /** Pick one of the presented cards (a revealed hand) by index into {@code cards}. */
    public static InteractionPromptMessage cardIndexPick(List<CardView> cards,
                                                         List<Integer> validIndices, String prompt,
                                                         boolean declinable) {
        return of(InteractionShape.CARD_INDEX_PICK, prompt, validIndices, cards, null, null, null,
                null, null, declinable, null, null, null, null, null);
    }

    public static InteractionPromptMessage graveyardIndexPick(List<Integer> cardIndices,
                                                              String prompt, boolean allGraveyards) {
        return of(InteractionShape.GRAVEYARD_INDEX_PICK, prompt, cardIndices, null, null, null,
                null, null, null, null, null, null, null, allGraveyards, null);
    }

    public static InteractionPromptMessage libraryIndexPick(List<CardView> cards, String prompt,
                                                            boolean canFailToFind) {
        return of(InteractionShape.LIBRARY_INDEX_PICK, prompt, null, cards, null, null, null,
                null, null, canFailToFind, null, null, null, null, null);
    }

    public static InteractionPromptMessage permanentPick(List<UUID> permanentIds,
                                                         List<UUID> playerIds, String prompt) {
        return of(InteractionShape.PERMANENT_PICK, prompt, null, null, null, permanentIds,
                playerIds, null, null, null, null, null, null, null, null);
    }

    public static InteractionPromptMessage multiCardPick(List<UUID> cardIds, List<CardView> cards,
                                                         int maxCount, String prompt) {
        return of(InteractionShape.MULTI_CARD_PICK, prompt, null, cards, cardIds, null, null,
                null, maxCount, null, null, null, null, null, null);
    }

    public static InteractionPromptMessage multiPermanentPick(List<UUID> permanentIds, int maxCount,
                                                              String prompt) {
        return of(InteractionShape.MULTI_PERMANENT_PICK, prompt, null, null, null, permanentIds,
                null, null, maxCount, null, null, null, null, null, null);
    }

    public static InteractionPromptMessage listPick(List<String> options, String prompt,
                                                    boolean searchable) {
        return of(InteractionShape.LIST_PICK, prompt, null, null, null, null, null, options,
                null, null, null, null, null, null, searchable);
    }

    public static InteractionPromptMessage acceptDecline(String prompt, boolean canPay,
                                                         String manaCost) {
        return of(InteractionShape.ACCEPT_DECLINE, prompt, null, null, null, null, null, null,
                null, null, canPay, manaCost, null, null, null);
    }

    public static InteractionPromptMessage numberPick(String prompt, int maxValue, String cardName) {
        return of(InteractionShape.NUMBER_PICK, prompt, null, null, null, null, null, null,
                maxValue, null, null, null, cardName, null, null);
    }

    public static InteractionPromptMessage scryOrder(List<CardView> cards, String prompt) {
        return scryOrder(cards, prompt, false);
    }

    /**
     * Scry / surveil order prompt. {@code toGraveyard} (carried in the {@code allGraveyards}
     * field) tells the client the reject pile is the graveyard (surveil) rather than the bottom
     * of the library (scry), so it can label the second pile accordingly.
     */
    public static InteractionPromptMessage scryOrder(List<CardView> cards, String prompt,
                                                     boolean toGraveyard) {
        return of(InteractionShape.SCRY_ORDER, prompt, null, cards, null, null, null, null,
                null, null, null, null, null, toGraveyard ? Boolean.TRUE : null, null);
    }

    public static InteractionPromptMessage cardOrder(List<CardView> cards, String prompt) {
        return of(InteractionShape.CARD_ORDER, prompt, null, cards, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    public static InteractionPromptMessage handTopBottom(List<CardView> cards, String prompt) {
        return of(InteractionShape.HAND_TOP_BOTTOM, prompt, null, cards, null, null, null, null,
                null, null, null, null, null, null, null);
    }
}
