package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.InteractionShape;
import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

/**
 * The single client-to-server answer for every pending interaction. The {@link InteractionShape}
 * selects which fields carry the answer (the rest are null), mirroring the shape of the
 * {@link InteractionPromptMessage} that asked:
 *
 * <ul>
 *   <li>{@code CARD_INDEX_PICK} / {@code GRAVEYARD_INDEX_PICK} / {@code LIBRARY_INDEX_PICK} —
 *       {@code index} (−1 declines where the prompt was declinable)</li>
 *   <li>{@code PERMANENT_PICK} — {@code id}</li>
 *   <li>{@code MULTI_CARD_PICK} / {@code MULTI_PERMANENT_PICK} — {@code ids}</li>
 *   <li>{@code LIST_PICK} — {@code choice}</li>
 *   <li>{@code ACCEPT_DECLINE} — {@code accepted}</li>
 *   <li>{@code NUMBER_PICK} — {@code number}</li>
 *   <li>{@code CARD_ORDER} — {@code order}</li>
 *   <li>{@code SCRY_ORDER} — {@code order} (top) and {@code secondOrder} (bottom)</li>
 *   <li>{@code HAND_TOP_BOTTOM} — {@code index} (to hand) and {@code secondIndex} (to top)</li>
 * </ul>
 */
public record InteractionAnswerRequest(
        MessageType type,
        InteractionShape shape,
        Integer index,
        Integer secondIndex,
        UUID id,
        List<UUID> ids,
        String choice,
        Boolean accepted,
        Integer number,
        List<Integer> order,
        List<Integer> secondOrder) {
}
