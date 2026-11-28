package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.EumidianWastewakerState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EumidianWastewakerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.CardChoiceHandlerService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Resolves Eumidian Wastewaker's "you and defending player each discard a card or sacrifice a permanent. You draw a
 * card for each land card put into a graveyard this way."
 *
 * <p>You, then the defending player, each choose to discard or to sacrifice (a player who can do only one of them
 * does that) and choose the card or permanent; nothing happens until both have chosen, and then the discards and the
 * sacrifice happen together (the card's ruling, CR 101.4). The discard picks wait in
 * {@link GameData#deferredDiscardSelections}; the sacrifice picks in {@link EumidianWastewakerState}. Once all have
 * chosen, the chosen permanents' CR 616.1 death replacement questions are asked before anything happens (marked
 * {@link AsksDeathReplacementsFirst}: until then it changes nothing but its recorded picks, so the rerun resumes at the
 * same point); then the discards are made, with every chosen permanent still on the battlefield, so a replacement of a
 * permanent being sacrificed still applies to them, and the chosen permanents leave as one departure with those
 * answers. Only land cards that end up in a graveyard are counted: a land token,
 * a card that is a land only on the battlefield (Ashaya, Soul of the Wild) and a card a replacement put elsewhere are
 * not (the card's ruling).
 */
@Component
@RequiredArgsConstructor
public class EumidianWastewakerEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final StepDepartureSupport stepDepartureSupport;

    @Autowired
    @Lazy
    private CardChoiceHandlerService cardChoiceHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EumidianWastewakerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        EumidianWastewakerState state = gameData.eumidianWastewaker;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.remaining.add(entry.getControllerId());
            UUID defendingPlayerId = defendingPlayerId(gameData, entry);
            if (defendingPlayerId != null && !defendingPlayerId.equals(entry.getControllerId())) {
                state.remaining.add(defendingPlayerId);
            }
        } else if (state.sacrificing) {
            finish(gameData, entry, state);
            return;
        }
        // Back after a discard pick: the pick is recorded, so the player is done choosing.
        gameData.deferredDiscardChooserId = null;

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            if (choose(gameData, entry, sourceName, state, chosenMode)) {
                return;
            }
        }

        advance(gameData, entry, effect, sourceName, state);
    }

    /** Asks the next player to choose; once every player has chosen, everything happens together. */
    private void advance(GameData gameData, StackEntry entry, CardEffect effect, String sourceName,
                         EumidianWastewakerState state) {
        while (!state.remaining.isEmpty()) {
            UUID playerId = state.remaining.removeFirst();
            if (!gameData.playerIds.contains(playerId)) {
                continue;
            }
            state.currentPlayerId = playerId;

            boolean hasPermanent = !sacrificeablePermanentIds(gameData, playerId, entry.getControllerId()).isEmpty();
            boolean hasCard = !unchosenHandIndices(gameData, playerId).isEmpty();
            if (hasPermanent && hasCard) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new ChoiceContext.EachPlayerSacrificeOrDiscardChoice(playerId, sourceName),
                        List.of(
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE,
                                ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD
                        ),
                        sourceName + " — sacrifice a permanent or discard a card."
                ));
                return;
            }
            if (hasPermanent || hasCard) {
                String mode = hasPermanent
                        ? ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE
                        : ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD;
                if (choose(gameData, entry, sourceName, state, mode)) {
                    return;
                }
            }
        }

        actTogether(gameData, entry, effect, state);
    }

    /**
     * The current player chooses the permanent to sacrifice or the card to discard, without either happening yet; a
     * lone permanent is chosen unasked. Returns whether the player is being asked.
     */
    private boolean choose(GameData gameData, StackEntry entry, String sourceName, EumidianWastewakerState state,
                           String mode) {
        UUID playerId = state.currentPlayerId;
        if (ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE.equals(mode)) {
            List<UUID> ids = sacrificeablePermanentIds(gameData, playerId, entry.getControllerId());
            if (ids.size() == 1) {
                state.chosenSacrificeIds.add(ids.getFirst());
            } else if (!ids.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = true;
                PermanentChoiceContext.EumidianWastewakerSacrifice context =
                        new PermanentChoiceContext.EumidianWastewakerSacrifice(playerId);
                gameData.interaction.setPermanentChoiceContext(context);
                playerInputService.beginPermanentChoice(gameData, playerId, ids, context,
                        sourceName + " — choose a permanent to sacrifice.");
                return true;
            }
            return false;
        }

        List<Integer> indices = unchosenHandIndices(gameData, playerId);
        if (indices.isEmpty()) {
            return false;
        }
        gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
        gameData.rerunCurrentEffectAfterInteraction = true;
        gameData.deferredDiscardChooserId = playerId;
        playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, indices);
        if (gameData.interaction.isAwaitingInput()) {
            return true;
        }
        gameData.deferredDiscardChooserId = null;
        return false;
    }

    /**
     * Every player has chosen: the chosen cards are discarded, then the chosen permanents are sacrificed as one
     * departure. The land cards discarded into a graveyard are counted now; the sacrificed ones once they are gone.
     */
    private void actTogether(GameData gameData, StackEntry entry, CardEffect effect, EumidianWastewakerState state) {
        // CR 616.1: the sacrifices' death replacements are chosen before anything is discarded or sacrificed.
        destructionSupport.askDeathReplacementsBeforeSacrifice(gameData, state.chosenSacrificeIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(java.util.Objects::nonNull)
                .toList());
        gameData.deathReplacementsCanRerunEffect = false;
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.currentPlayerId = null;
        Map<UUID, List<Card>> discarded =
                cardChoiceHandlerService.discardDeferredSelections(gameData, entry.getControllerId());
        gameData.discardCausedByOpponent = false;
        discarded.values().forEach(cards -> cards.forEach(card -> {
            if (isLandCardInGraveyard(gameData, card.getId())) {
                state.landCardsPutIntoGraveyard++;
            }
        }));

        List<UUID> sacrifices = new ArrayList<>();
        for (UUID permanentId : state.chosenSacrificeIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                sacrifices.add(permanentId);
                state.sacrificedCardIds.add(permanent.getCard().getId());
            }
        }
        if (sacrifices.isEmpty()) {
            finish(gameData, entry, state);
            return;
        }
        state.sacrificing = true;
        // A step departure: a CR 616.1 question ends the step before anything leaves, and the effect is resolved
        // again to count and draw once the sacrifice is done.
        if (stepDepartureSupport.departOrResolveAgain(gameData, entry, effect,
                DepartureBatch.of(StepDepartureSupport.sacrifices(sacrifices)))) {
            finish(gameData, entry, state);
        }
    }

    private void finish(GameData gameData, StackEntry entry, EumidianWastewakerState state) {
        int drawAmount = state.landCardsPutIntoGraveyard
                + (int) state.sacrificedCardIds.stream()
                .filter(cardId -> isLandCardInGraveyard(gameData, cardId))
                .count();
        entry.setEventValue(drawAmount);
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (drawAmount > 0) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), drawAmount);
        }
    }

    /** Whether the card is now a land card in a graveyard; a token is no card (the card's ruling). */
    private boolean isLandCardInGraveyard(GameData gameData, UUID cardId) {
        Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
        return card != null && !card.isToken() && card.hasType(CardType.LAND);
    }

    private List<UUID> sacrificeablePermanentIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        return gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)
                ? destructionSupport.collectPermanentIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent))
                : List.of();
    }

    /** Positions in the player's hand of the cards not already chosen to be discarded. */
    private List<Integer> unchosenHandIndices(GameData gameData, UUID playerId) {
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            UUID cardId = hand.get(i).getId();
            if (gameData.deferredDiscardSelections.stream().noneMatch(pick -> pick.cardId().equals(cardId))) {
                indices.add(i);
            }
        }
        return indices;
    }

    private UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        UUID attackedTargetId = entry.getAttackedTargetId() != null
                ? entry.getAttackedTargetId() : entry.getTargetId();
        if (attackedTargetId == null) {
            return null;
        }
        return gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
    }
}
