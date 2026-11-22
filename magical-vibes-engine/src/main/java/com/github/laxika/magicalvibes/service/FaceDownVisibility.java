package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.event.VisualCueService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The one rule for whether a spell or ability on the stack gives away a face-down card (CR 708.2, 708.5): the stack
 * views, the name prompts, the game log and every text that names such an object ask it.
 */
public final class FaceDownVisibility {

    private static final String FACE_DOWN_CARD = "a face-down card";

    private FaceDownVisibility() {
    }

    /**
     * Whether the stack shows the name of {@code entry}'s card: not a spell cast face down (CR 708.4), nor the source
     * of an ability that is or last was face down on the battlefield (CR 708.2) or lies face down in exile (CR 406.3).
     */
    public static boolean showsNameOnStack(GameData gameData, StackEntry entry) {
        Card card = entry.getCard();
        if (card == null || entry.isCastFaceDown()) {
            return false;
        }
        if (!isAbility(entry)) {
            return true;
        }
        if (gameData.isExiledFaceDown(card.getId()) || VisualCueService.Source.of(gameData, entry).name() == null) {
            return false;
        }
        // An ability may name its source by the card alone, without the permanent.
        return findFaceDownPermanent(gameData, card.getId()) == null;
    }

    /**
     * Whether {@code viewerId}, or nobody in particular when null, may see {@code entry}'s card: everybody when the
     * stack shows its name, else the player who may look at it, which is the controller of the face-down spell or
     * permanent (CR 708.5) or a player allowed to look at the exiled card (CR 406.3), and whoever controls that
     * player (CR 724.4).
     */
    public static boolean maySeeOnStack(GameData gameData, StackEntry entry, UUID viewerId) {
        Card card = entry.getCard();
        if (card == null || showsNameOnStack(gameData, entry)) {
            return true;
        }
        if (viewerId == null) {
            return false;
        }
        if (maySeeAs(gameData, entry, viewerId)) {
            return true;
        }
        return viewerId.equals(gameData.mindControllerPlayerId()) && gameData.mindControlledPlayerId() != null
                && maySeeAs(gameData, entry, gameData.mindControlledPlayerId());
    }

    private static boolean maySeeAs(GameData gameData, StackEntry entry, UUID playerId) {
        if (entry.isCastFaceDown()) {
            return playerId.equals(entry.getControllerId());
        }
        UUID cardId = entry.getCard().getId();
        if (gameData.isExiledFaceDown(cardId)) {
            return gameData.mayLookAtExiledCard(cardId, playerId);
        }
        Permanent source = faceDownSource(gameData, entry);
        return source != null && gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .anyMatch(permanent -> permanent.getId().equals(source.getId()));
    }

    /**
     * The face-down permanent that is the source of the ability {@code entry}, as it is on the battlefield or else as
     * it last was there; null when there is none.
     */
    public static Permanent faceDownSource(GameData gameData, StackEntry entry) {
        if (!isAbility(entry)) {
            return null;
        }
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId != null) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getId().equals(sourceId) && permanent.isFaceDown()) {
                        return permanent;
                    }
                }
            }
        }
        Card card = entry.getCard();
        Permanent byCard = card == null ? null : findFaceDownPermanent(gameData, card.getId());
        if (byCard != null) {
            return byCard;
        }
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        return snapshot != null && snapshot.isFaceDown() ? snapshot : null;
    }

    /** The name of {@code entry}'s card as {@code viewerId}, or everybody when null, may be told it. */
    public static String nameShownTo(GameData gameData, StackEntry entry, UUID viewerId) {
        if (maySeeOnStack(gameData, entry, viewerId)) {
            return entry.getCard().getName();
        }
        return entry.isCastFaceDown() ? StackEntry.FACE_DOWN_SPELL : FACE_DOWN_CARD;
    }

    /** {@code entry}'s description as {@code viewerId}, or everybody when null, may be told it. */
    public static String descriptionShownTo(GameData gameData, StackEntry entry, UUID viewerId) {
        if (maySeeOnStack(gameData, entry, viewerId)) {
            return entry.getDescription();
        }
        if (entry.isCastFaceDown()) {
            return StackEntry.FACE_DOWN_SPELL;
        }
        return "an ability of " + FACE_DOWN_CARD;
    }

    /** {@code card}'s name where everybody reads it, for a card that may be a face-down spell or permanent. */
    public static String publicName(GameData gameData, Card card) {
        String hidden = faceDownLabel(gameData, card.getId());
        return hidden == null ? card.getName() : hidden;
    }

    /**
     * {@code logEntry} as the log, which every player reads, keeps it: with no card of a face-down spell on the stack
     * or of a face-down permanent. A card carries its printing, so even a nameless copy of its face-down
     * characteristics would identify it.
     */
    static GameLogEntry redact(GameData gameData, GameLogEntry logEntry) {
        List<GameLogSegment> segments = new ArrayList<>(logEntry.segments());
        boolean changed = false;
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i) instanceof GameLogSegment.CardSegment segment) {
                String hidden = faceDownLabel(gameData, segment.card().getId());
                if (hidden != null) {
                    boolean startsSentence = i == 0
                            || segments.get(i - 1) instanceof GameLogSegment.Text text && text.value().endsWith(". ");
                    segments.set(i, GameLogSegment.text(startsSentence ? capitalized(hidden) : hidden));
                    changed = true;
                }
            }
        }
        return changed ? new GameLogEntry(segments) : logEntry;
    }

    private static String capitalized(String text) {
        return Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    private static String faceDownLabel(GameData gameData, UUID cardId) {
        for (StackEntry entry : gameData.stack) {
            if (!isAbility(entry) && entry.getCard() != null && cardId.equals(entry.getCard().getId())) {
                // The spell decides: one cast from the battlefield with spellmorph is face up before it leaves there.
                return entry.isCastFaceDown() ? StackEntry.FACE_DOWN_SPELL : null;
            }
        }
        return findFaceDownPermanent(gameData, cardId) == null ? null : "a face-down permanent";
    }

    private static Permanent findFaceDownPermanent(GameData gameData, UUID cardId) {
        return gameData.playerBattlefields.values().stream()
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(permanent -> permanent.isFaceDown()
                        && (cardId.equals(permanent.getCard().getId())
                        || cardId.equals(permanent.getOriginalCard().getId())))
                .findFirst()
                .orElse(null);
    }

    private static boolean isAbility(StackEntry entry) {
        return entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY;
    }
}
