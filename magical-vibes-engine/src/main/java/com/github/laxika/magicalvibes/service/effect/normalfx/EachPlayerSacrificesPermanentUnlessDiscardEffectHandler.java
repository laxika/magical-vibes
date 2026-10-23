package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.EachPlayerSacrificeOrDiscardState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesPermanentUnlessDiscardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves an APNAP each-player choice to discard a card or sacrifice a permanent.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst} for the collected form (Possessed Portal): its only
 * removal is the final simultaneous sacrifice, whose competing death replacements (CR 616.1) are
 * asked while the progress on {@link EachPlayerSacrificeOrDiscardState} is still recorded, so the
 * rerun resumes at the same batch and repeats nothing. The per-card-drawn form (Read the Runes)
 * sacrifices as it goes and resolves with the rerun window closed.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesPermanentUnlessDiscardEffectHandler implements NormalEffectHandlerBean,
        AsksDeathReplacementsFirst {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesPermanentUnlessDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EachPlayerSacrificesPermanentUnlessDiscardEffect sacrificeEffect =
                (EachPlayerSacrificesPermanentUnlessDiscardEffect) effect;
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        String sourceName = entry.getCard().getName();
        if (sacrificeEffect.repeatForEachCardDrawn()) {
            // Each sacrifice happens as it is chosen, after the progress has moved on, so a rerun
            // would skip it.
            gameData.deathReplacementsCanRerunEffect = false;
        }

        if (!state.active) {
            state.reset();
            state.active = true;
            state.deferSacrifices = !sacrificeEffect.repeatForEachCardDrawn();
            if (sacrificeEffect.repeatForEachCardDrawn()) {
                for (int i = 0; i < entry.getDrawnCardIdsThisResolution().size(); i++) {
                    state.remaining.add(entry.getControllerId());
                }
            } else {
                state.remaining.addAll(apnapPlayers(gameData));
            }
            advance(gameData, entry, sourceName);
            return;
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, chosenMode);
            return;
        }

        advance(gameData, entry, sourceName);
    }

    private void advance(GameData gameData, StackEntry entry, String sourceName) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            state.currentPlayerId = playerId;

            boolean hasPermanent = !permanentIds(gameData, playerId, entry.getControllerId()).isEmpty();
            boolean hasCard = hasCardToDiscard(gameData, playerId);
            if (!hasPermanent && !hasCard) {
                continue;
            }
            // "Sacrifice a permanent unless you discard a card": discarding is optional, so a player
            // with a card but no permanent may still keep it. When the discard is the
            // default ("discard unless you sacrifice"), a player with nothing to sacrifice must discard.
            boolean discardIsOptional = !repeatsPerCardDrawn(entry);
            if (hasCard && (hasPermanent || discardIsOptional)) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new ChoiceContext.EachPlayerSacrificeOrDiscardChoice(playerId, sourceName),
                        List.of(
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE,
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD
                        ),
                        sourceName + " — sacrifice a permanent unless you discard a card."
                ));
                return;
            }

            applyMode(gameData, entry, sourceName,
                    hasPermanent
                            ? ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE
                            : ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
            return;
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        List<UUID> sacrifices = List.copyOf(state.chosenSacrifices);
        // CR 616.1: the chosen permanents' death replacements are asked before any of them is
        // sacrificed, while the progress is still recorded, so a rerun resumes right here.
        destructionSupport.askDeathReplacementsBeforeSacrifice(gameData, sacrifices.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(Objects::nonNull)
                .toList());
        gameData.deathReplacementsCanRerunEffect = false;
        state.reset();
        if (!sacrifices.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, sacrifices);
        }
    }

    private static boolean repeatsPerCardDrawn(StackEntry entry) {
        return entry.getEffectsToResolve().stream().anyMatch(effect ->
                effect instanceof EachPlayerSacrificesPermanentUnlessDiscardEffect sacrifice
                        && sacrifice.repeatForEachCardDrawn());
    }

    private void applyMode(GameData gameData, StackEntry entry, String sourceName, String mode) {
        EachPlayerSacrificeOrDiscardState state = gameData.eachPlayerSacrificeOrDiscard;
        UUID playerId = state.currentPlayerId;

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE.equals(mode)) {
            List<UUID> ids = permanentIds(gameData, playerId, entry.getControllerId());
            if (ids.isEmpty()) {
                advance(gameData, entry, sourceName);
                return;
            }
            if (ids.size() == 1) {
                Permanent permanent = gameData.playerBattlefields.get(playerId).stream()
                        .filter(candidate -> candidate.getId().equals(ids.getFirst()))
                        .findFirst()
                        .orElse(null);
                if (permanent != null && state.deferSacrifices) {
                    state.chosenSacrifices.add(permanent.getId());
                } else if (permanent != null) {
                    destructionSupport.sacrificeAndLog(gameData, permanent, playerId);
                }
                advance(gameData, entry, sourceName);
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(playerId));
            playerInputService.beginPermanentChoice(gameData, playerId, ids,
                    sourceName + " — choose a permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD.equals(mode)) {
            if (!hasCardToDiscard(gameData, playerId)) {
                advance(gameData, entry, sourceName);
                return;
            }
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                advance(gameData, entry, sourceName);
            }
            return;
        }

        advance(gameData, entry, sourceName);
    }

    private List<UUID> permanentIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        return gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)
                ? destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent))
                : List.of();
    }

    private boolean hasCardToDiscard(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.get(playerId);
        return hand != null && !hand.isEmpty();
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> players = new ArrayList<>();
        if (gameData.activePlayerId != null && gameData.playerIds.contains(gameData.activePlayerId)) {
            players.add(gameData.activePlayerId);
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!players.contains(playerId) && gameData.playerIds.contains(playerId)) {
                players.add(playerId);
            }
        }
        return players;
    }
}
