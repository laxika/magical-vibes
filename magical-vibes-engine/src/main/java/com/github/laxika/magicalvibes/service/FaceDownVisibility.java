package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.GameLogSegment;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardFaceDownInsteadOfDrawReplacement;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryForCardsToExileFaceDownPileEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.service.event.VisualCueService;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/**
 * The one rule for whether a spell or ability on the stack, or a permanent, gives away a face-down card (CR 708.2,
 * 708.5): the stack views, the name prompts, the game log and every text that names such an object ask it.
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

    /**
     * Whether {@code viewerId}, or nobody in particular when null, may look at {@code permanent}'s card: everybody
     * while it is face up, else the player who controls it now, phased out or not (CR 708.5), and whoever controls that
     * player (CR 724.4).
     */
    public static boolean maySee(GameData gameData, Permanent permanent, UUID viewerId) {
        if (!permanent.isFaceDown()) {
            return true;
        }
        if (viewerId == null) {
            return false;
        }
        return controls(gameData, viewerId, permanent)
                || viewerId.equals(gameData.mindControllerPlayerId()) && gameData.mindControlledPlayerId() != null
                && controls(gameData, gameData.mindControlledPlayerId(), permanent);
    }

    private static boolean maySeeAs(GameData gameData, StackEntry entry, UUID playerId) {
        if (entry.isCastFaceDown()) {
            return playerId.equals(entry.getControllerId());
        }
        UUID cardId = entry.getCard().getId();
        if (gameData.isExiledFaceDown(cardId)) {
            return mayLookAtExiledCard(gameData, cardId, playerId);
        }
        Permanent source = faceDownSource(gameData, entry);
        return source != null && controls(gameData, playerId, source);
    }

    /**
     * Whether {@code playerId} may look at the card {@code cardId} that is exiled face down (CR 406.3): one of
     * {@link #exileViewers(GameData, UUID)}. Every view, prompt and log line that may show such a card asks this.
     */
    public static boolean mayLookAtExiledCard(GameData gameData, UUID cardId, UUID playerId) {
        return playerId != null && exileViewers(gameData, cardId).contains(playerId);
    }

    /**
     * The players who may look at the face-down exiled card {@code cardId}, or none when it isn't exiled face down:
     * see {@link #exileViewers(GameData, Permanent, UUID, ExiledCardEntry)}, with the permanent it was exiled with
     * found on the battlefield.
     */
    public static Set<UUID> exileViewers(GameData gameData, UUID cardId) {
        ExiledCardEntry exiled = gameData.findExiledCard(cardId);
        if (exiled == null || !exiled.faceDown()) {
            return Set.of();
        }
        for (UUID controllerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
                if (permanent.getId().equals(exiled.sourcePermanentId())
                        || gameData.imprintedCards.get(permanent.getCard().getId()) == exiled.card()) {
                    return exileViewers(gameData, permanent, controllerId, exiled);
                }
            }
        }
        return exileViewers(gameData, null, null, exiled);
    }

    /**
     * The players who may look at {@code exiled}, a card exiled face down (CR 406.3), the one answer the game view and
     * every prompt read: each player a resolution or control change granted a look ({@code GameData.exileLookViewers}),
     * and, while {@code source} (the permanent it was exiled with, controlled by {@code sourceControllerId}) is on the
     * battlefield, the player who exiled it or else that controller, unless the source's abilities exile it so that
     * nobody may look: a draw replaced by a face-down exile, a face-down pile, a face-down imprint from the hand, an
     * intel counter, a face-down library exile without "you may look", or a hand exiled face down.
     */
    public static Set<UUID> exileViewers(GameData gameData, Permanent source, UUID sourceControllerId,
                                         ExiledCardEntry exiled) {
        Set<UUID> viewers = new LinkedHashSet<>();
        if (source != null && grantsImplicitLook(gameData, source.getCard(), exiled)) {
            viewers.add(exiled.exilerId() != null ? exiled.exilerId() : sourceControllerId);
        }
        viewers.addAll(gameData.exileLookViewers(exiled.card().getId()));
        return viewers;
    }

    private static boolean grantsImplicitLook(GameData gameData, Card source, ExiledCardEntry exiled) {
        if (source.getStaticEffects().stream().anyMatch(ExileTopCardFaceDownInsteadOfDrawReplacement.class::isInstance)
                || source.getOwnEntryEffects().stream()
                .anyMatch(SearchLibraryForCardsToExileFaceDownPileEffect.class::isInstance)
                || gameData.exiledCardHasCounter(exiled.card().getId(), CounterType.INTEL)) {
            return false;
        }
        List<CardEffect> effects = Stream.of(source.getCastingCostsAndSpellEffects().stream(),
                        source.getStaticEffects().stream(), source.getTriggeredEffects().stream(),
                        source.getOwnEntryEffects().stream(),
                        source.getActivatedAbilities(Zone.BATTLEFIELD).stream()
                                .flatMap(ability -> ability.getEffects().stream()))
                .flatMap(stream -> stream)
                .flatMap(FaceDownVisibility::unwrapped)
                .toList();
        return effects.stream().noneMatch(effect -> effect instanceof ExileFromHandToImprintEffect imprint
                        && imprint.faceDown()
                        || effect instanceof ExileTopCardsToSourceEffect exile
                        && exile.faceDown() && !exile.mayLookAtFaceDownCards()
                        || effect instanceof ExileTargetPlayerHandEffect hand && hand.faceDown());
    }

    /** {@code effect} and the steps it wraps: a "you may" choice's effect, a sequence's steps. */
    public static Stream<CardEffect> unwrapped(CardEffect effect) {
        return switch (effect) {
            case MayEffect may -> Stream.concat(Stream.of(effect), unwrapped(may.wrapped()));
            case SequenceEffect sequence -> Stream.concat(Stream.of(effect),
                    sequence.steps().stream().flatMap(FaceDownVisibility::unwrapped));
            default -> Stream.of(effect);
        };
    }

    /** Whether {@code playerId} controls {@code permanent}, on the battlefield or phased out. */
    private static boolean controls(GameData gameData, UUID playerId, Permanent permanent) {
        return Stream.of(gameData.playerBattlefields.get(playerId), gameData.phasedOutPermanents.get(playerId))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .anyMatch(candidate -> candidate.getId().equals(permanent.getId()));
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
            return Permanent.displayName(entry.getCard());
        }
        return entry.isCastFaceDown() ? StackEntry.FACE_DOWN_SPELL : FACE_DOWN_CARD;
    }

    /**
     * {@code permanent}'s name as {@code viewerId}, or everybody when null, may be told it: its card's to a player who
     * may look at it, so that player can tell their face-down permanents apart (CR 708.6), else
     * {@link #publicName(GameData, Permanent)}. Only for a text that player alone reads, such as their prompt.
     */
    public static String nameShownTo(GameData gameData, Permanent permanent, UUID viewerId) {
        return maySee(gameData, permanent, viewerId) ? Permanent.displayName(permanent.getCard())
                : publicName(gameData, permanent);
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
        return hidden == null ? Permanent.displayName(card) : hidden;
    }

    /**
     * The name of {@code entry}'s source where everybody reads it: the log, or what an effect is shown as granted by.
     */
    public static String sourceName(GameData gameData, StackEntry entry) {
        return nameShownTo(gameData, entry, null);
    }

    /**
     * {@code permanent}'s name where everybody reads it: none while it's face down on the battlefield or phased out
     * (CR 708.5). Once it has left, its card's name, which its owner reveals as it moves (CR 708.9).
     */
    public static String publicName(GameData gameData, Permanent permanent) {
        return permanent.isFaceDown() ? publicName(gameData, permanent.getCard())
                : Permanent.displayName(permanent.getCard());
    }

    /** {@link #publicName(GameData, Permanent)} for a permanent that is on the battlefield as the text is built. */
    public static String publicName(Permanent permanent) {
        return permanent.getPublicName();
    }

    /**
     * {@code logEntry} as the log, which every player reads, keeps it: with no card of a face-down spell on the stack
     * or of a face-down permanent, and a card with no name described ({@link Permanent#displayName}). A card carries its printing, so even a nameless copy of its face-down
     * characteristics would identify it.
     */
    static GameLogEntry redact(GameData gameData, GameLogEntry logEntry) {
        List<GameLogSegment> segments = new ArrayList<>(logEntry.segments());
        boolean changed = false;
        if (!segments.isEmpty() && segments.getFirst() instanceof GameLogSegment.Text text
                && text.value().startsWith("a face-down ")) {
            // A label that text built for a hidden name begins the sentence.
            segments.set(0, GameLogSegment.text(capitalized(text.value())));
            changed = true;
        }
        for (int i = 0; i < segments.size(); i++) {
            if (segments.get(i) instanceof GameLogSegment.CardSegment segment) {
                String hidden = faceDownLabel(gameData, segment.card().getId());
                if (hidden == null && !Objects.equals(segment.card().getName(), Permanent.displayName(segment.card()))) {
                    hidden = Permanent.displayName(segment.card());
                }
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
        return findFaceDownPermanent(gameData, cardId) == null ? null : Permanent.FACE_DOWN_PERMANENT;
    }

    private static Permanent findFaceDownPermanent(GameData gameData, UUID cardId) {
        return Stream.concat(gameData.playerBattlefields.values().stream(),
                        gameData.phasedOutPermanents.values().stream())
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
