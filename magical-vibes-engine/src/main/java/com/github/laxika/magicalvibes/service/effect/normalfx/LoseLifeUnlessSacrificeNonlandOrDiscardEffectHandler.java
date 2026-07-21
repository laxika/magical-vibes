package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TormentState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link LoseLifeUnlessSacrificeNonlandOrDiscardEffect}: the player on the stack entry's
 * {@code targetId} (the enchanted player, for a Curse upkeep trigger) loses N life unless they
 * sacrifice a nonland permanent of their choice or discard a card.
 *
 * <p>The single-player, single-pass slice of {@link TormentOfHailfireEffectHandler}: it reuses the
 * same penalty-choice plumbing ({@link ChoiceContext.TormentPenaltyChoice} /
 * {@link PermanentChoiceContext.TormentSacrifice}) and the shared {@link GameData#torment} state
 * ({@code chosenMode} is written back by {@code handleTormentPenaltyChoice}). The offered options are
 * pruned to what the affected player can do (life is always offered); a player whose only option is to
 * lose life takes it immediately without a prompt. Otherwise a three-way
 * {@link PendingInteraction.ColorChoice} list-pick is begun; the chosen mode then runs a sacrifice or
 * discard sub-choice (each re-runs this handler on completion via
 * {@link GameData#rerunCurrentEffectAfterInteraction}), or applies the life loss inline.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoseLifeUnlessSacrificeNonlandOrDiscardEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final DestructionSupport destructionSupport;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeUnlessSacrificeNonlandOrDiscardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeUnlessSacrificeNonlandOrDiscardEffect) effect;
        int lifeLoss = e.lifeLoss();
        TormentState state = gameData.torment;
        String sourceName = entry.getCard().getName();

        // Resolve the affected player (and, for the TARGET_PERMANENT_CONTROLLER variant, the targeted
        // permanent that must be excluded from the sacrifice options — "another nonland permanent").
        UUID excludedPermanentId = null;
        UUID playerId;
        if (e.recipient() == LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER) {
            excludedPermanentId = entry.getTargetId();
            playerId = excludedPermanentId == null
                    ? null
                    : gameQueryService.findPermanentController(gameData, excludedPermanentId);
        } else {
            playerId = entry.getTargetId();
        }
        if (playerId == null || !gameData.playerIds.contains(playerId)) {
            finish(gameData, state);
            return;
        }

        if (!state.active) {
            // Fresh entry: offer the pruned penalty options (life is always available).
            state.reset();
            state.active = true;

            List<String> options = availableOptions(gameData, playerId, lifeLoss, excludedPermanentId);
            if (options.size() == 1) {
                // Only "lose life" is possible — no choice to make.
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, sourceName);
                gameOutcomeService.checkWinCondition(gameData);
                finish(gameData, state);
                return;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName + " — lose " + lifeLoss
                    + " life unless you sacrifice a nonland permanent or discard a card.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    playerId, null, null,
                    new ChoiceContext.TormentPenaltyChoice(playerId, sourceName),
                    options, prompt));
            return;
        }

        if (state.chosenMode != null) {
            // The affected player just picked a penalty option — apply it.
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, lifeLoss, playerId, mode, state, excludedPermanentId);
            return;
        }

        // Re-entry after a sacrifice / discard sub-choice completed — the effect is done.
        finish(gameData, state);
    }

    /**
     * Applies the affected player's chosen penalty. Sacrifice/discard begin a sub-choice and pause
     * (this handler re-runs on completion, then finishes); losing life applies immediately.
     */
    private void applyMode(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            UUID playerId, String mode, TormentState state, UUID excludedPermanentId) {
        if (ChoiceContext.TormentPenaltyChoice.SACRIFICE.equals(mode)) {
            List<UUID> nonlandIds = nonlandPermanentIds(gameData, playerId, excludedPermanentId);
            if (nonlandIds.isEmpty()) {
                finish(gameData, state);
                return;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(playerId));
            playerInputService.beginPermanentChoice(gameData, playerId, nonlandIds,
                    sourceName + " — choose a nonland permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.TormentPenaltyChoice.DISCARD.equals(mode)) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null || hand.isEmpty()) {
                finish(gameData, state);
                return;
            }
            gameData.discardCausedByOpponent = !playerId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, playerId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                // Defensive: the hand emptied out from under us — just finish.
                finish(gameData, state);
            }
            return;
        }

        // "Lose N life": the player declined to sacrifice or discard.
        lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, sourceName);
        gameOutcomeService.checkWinCondition(gameData);
        finish(gameData, state);
    }

    private void finish(GameData gameData, TormentState state) {
        gameData.rerunCurrentEffectAfterInteraction = false;
        state.reset();
    }

    private List<String> availableOptions(GameData gameData, UUID playerId, int lifeLoss, UUID excludedPermanentId) {
        List<String> options = new ArrayList<>();
        if (!nonlandPermanentIds(gameData, playerId, excludedPermanentId).isEmpty()) {
            options.add(ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        if (hand != null && !hand.isEmpty()) {
            options.add(ChoiceContext.TormentPenaltyChoice.DISCARD);
        }
        options.add("Lose " + lifeLoss + " life");
        return options;
    }

    /**
     * Nonland permanents the player may sacrifice. {@code excludedPermanentId} (the targeted creature
     * for the TARGET_PERMANENT_CONTROLLER variant) is dropped so it can't satisfy "sacrifice
     * <em>another</em> nonland permanent"; it is {@code null} for the plain TARGET_PLAYER variant.
     */
    private List<UUID> nonlandPermanentIds(GameData gameData, UUID playerId, UUID excludedPermanentId) {
        return destructionSupport.collectPermanentIds(gameData, playerId,
                p -> !p.getCard().hasType(CardType.LAND) && !p.getId().equals(excludedPermanentId));
    }
}
