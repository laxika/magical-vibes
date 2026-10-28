package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAttackTargetsForCreatedTokensEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Makes the creature tokens a creation put onto the battlefield attacking and lets their controller choose what
 * each one attacks (CR 508.4), after the creation's replacement effects were ordered (CR 616.1). Token-creating
 * handlers call {@link #createdTokensAttack} right after the creation, so the step waits for a paused creation.
 */
@Component
@RequiredArgsConstructor
public class ChooseAttackTargetsForCreatedTokensEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseAttackTargetsForCreatedTokensEffect.class;
    }

    /**
     * Splices the attack step after the effect {@code entry} is resolving, so it runs once the creation that
     * began with {@code createdFromIndex} created permanents has finished, or runs it now when the entry isn't
     * resolving its effect list.
     */
    public void createdTokensAttack(GameData gameData, StackEntry entry, UUID controllerId, int createdFromIndex,
                                    boolean sacrificeAtEndStep) {
        createdTokensAttack(gameData, entry, controllerId, createdFromIndex, sacrificeAtEndStep, null);
    }

    /**
     * As {@link #createdTokensAttack(GameData, StackEntry, UUID, int, boolean)}, but when {@code attackTargetId}
     * is set every created creature token attacks that player or permanent without a choice.
     */
    public void createdTokensAttack(GameData gameData, StackEntry entry, UUID controllerId, int createdFromIndex,
                                    boolean sacrificeAtEndStep, UUID attackTargetId) {
        var step = new ChooseAttackTargetsForCreatedTokensEffect(controllerId, createdFromIndex, sacrificeAtEndStep,
                attackTargetId);
        if (entry.getResolvingEffectIndex() >= 0) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(step));
        } else {
            resolve(gameData, entry, step);
        }
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var step = (ChooseAttackTargetsForCreatedTokensEffect) effect;
        UUID controllerId = step.controllerId();
        List<UUID> createdIds = entry.getCreatedPermanentIds();
        boolean inCombat = controllerId.equals(gameData.activePlayerId)
                && gameData.currentStep != null && gameData.currentStep.isCombatPhase();
        List<UUID> attackingTokenIds = new ArrayList<>();
        for (int i = Math.min(step.createdFromIndex(), createdIds.size()); i < createdIds.size(); i++) {
            Permanent token = gameQueryService.findPermanentById(gameData, createdIds.get(i));
            if (token == null) {
                continue;
            }
            if (step.sacrificeAtEndStep()) {
                gameData.queueDelayedAction(new DelayedPermanentAction(token.getId(),
                        DelayedPermanentActionKind.SACRIFICE_AT_END_STEP, false, null, null, controllerId));
            }
            if (inCombat && gameQueryService.isCreature(gameData, token)) {
                token.enterAttacking(true);
                attackingTokenIds.add(token.getId());
            }
        }
        if (step.attackTargetId() != null) {
            for (UUID tokenId : attackingTokenIds) {
                gameQueryService.findPermanentById(gameData, tokenId).setAttackTarget(step.attackTargetId());
            }
            return;
        }
        chooseAttackTargets(gameData, controllerId, attackingTokenIds);
    }

    /**
     * Assigns each token in {@code tokenIds} its attack target in turn: a token with a single defending player,
     * planeswalker or battle to attack attacks it, one with none isn't attacking (CR 508.4a), and the first one
     * with a choice prompts {@code controllerId} through {@link PermanentChoiceContext.TokenAttackTargetChoice}.
     * Returns whether a prompt is pending.
     */
    public boolean chooseAttackTargets(GameData gameData, UUID controllerId, List<UUID> tokenIds) {
        List<UUID> opponentIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId)).toList();
        List<UUID> permanentTargetIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if ((gameQueryService.isPlaneswalker(gameData, permanent) && opponentIds.contains(playerId))
                    || (gameQueryService.isBattle(gameData, permanent)
                    && opponentIds.contains(permanent.getProtectorPlayerId()))) {
                permanentTargetIds.add(permanent.getId());
            }
        });
        for (int i = 0; i < tokenIds.size(); i++) {
            Permanent token = gameQueryService.findPermanentById(gameData, tokenIds.get(i));
            if (token == null) {
                continue;
            }
            if (opponentIds.size() + permanentTargetIds.size() <= 1) {
                if (opponentIds.isEmpty() && permanentTargetIds.isEmpty()) {
                    token.enterAttacking(false);
                } else {
                    token.setAttackTarget(opponentIds.isEmpty() ? permanentTargetIds.getFirst() : opponentIds.getFirst());
                }
                continue;
            }
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TokenAttackTargetChoice(
                    controllerId, tokenIds.subList(i, tokenIds.size())));
            playerInputService.beginAnyTargetChoice(gameData, controllerId, permanentTargetIds, opponentIds,
                    "Choose the player, planeswalker, or battle for the token to attack.");
            return true;
        }
        return false;
    }
}
