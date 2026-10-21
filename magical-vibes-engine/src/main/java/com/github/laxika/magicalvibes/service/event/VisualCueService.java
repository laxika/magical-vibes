package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The one way engine code emits a {@link GameEventFact.VisualCue}. It owns the audience default
 * and the redaction of hidden sources, so emitting chokepoints only say what happened.
 *
 * <p>Cues are cosmetic: no rules code may read them, and emitting one never changes the game.
 * Emitting is a no-op in a simulation copy (whose batches are never dispatched anyway) and
 * outside a mutation scope, such as test setup that moves cards directly, so a chokepoint never
 * has to know which context it runs in.
 */
@Service
@RequiredArgsConstructor
public class VisualCueService {

    private final GameMutationCoordinator mutationCoordinator;

    /**
     * Emits a cue to every player. Use this for anything that happened in a public zone.
     */
    public void emit(GameData gameData, VisualCueType type, Source source,
                     UUID targetPermanentId, UUID targetPlayerId, Integer amount) {
        emit(gameData, type, source, targetPermanentId, targetPlayerId, amount,
                GameEventAudience.allPlayers());
    }

    /**
     * Emits a cue to an explicit audience. A cue about a card in a hidden zone must name only
     * the players who may see that card.
     */
    public void emit(GameData gameData, VisualCueType type, Source source,
                     UUID targetPermanentId, UUID targetPlayerId, Integer amount,
                     GameEventAudience audience) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(audience, "audience");
        if (gameData.simulation || !mutationCoordinator.isInAction(gameData)) {
            return;
        }
        mutationCoordinator.emit(gameData,
                new GameEventFact.VisualCue(type, source.cardId(), source.name(), source.colors(),
                        targetPermanentId, targetPlayerId, amount),
                audience);
    }

    /**
     * The source of a cue as the client may see it. Build one with the static factories, which
     * redact what a face-down source hides.
     */
    public record Source(UUID cardId, String name, List<CardColor> colors) {

        private static final Source NONE = new Source(null, null, List.of());

        public Source {
            colors = colors == null ? List.of() : List.copyOf(colors);
        }

        /** No source, such as a state-based death. */
        public static Source none() {
            return NONE;
        }

        /** A face-up card in a public zone. */
        public static Source of(Card card) {
            return new Source(card.getId(), card.getName(), card.getColors());
        }

        /** A permanent; a face-down one keeps its card id but hides its name and colors. */
        public static Source of(Permanent permanent) {
            return permanent.isFaceDown()
                    ? hidden(permanent.getCard())
                    : of(permanent.getCard());
        }

        /** A spell or ability on the stack; a face-down spell hides its name and colors. */
        public static Source of(StackEntry entry) {
            return entry.isCastFaceDown()
                    ? hidden(entry.getCard())
                    : of(entry.getCard());
        }

        private static Source hidden(Card card) {
            return new Source(card.getId(), null, List.of());
        }
    }
}
