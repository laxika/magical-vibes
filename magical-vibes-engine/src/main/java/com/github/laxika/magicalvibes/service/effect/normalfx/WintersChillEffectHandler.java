package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.WintersChillState;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WintersChillEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link WintersChillEffect}: for each targeted attacking creature, its controller may
 * pay {1} or {2}. Pay nothing → schedule end-of-combat destruction. Pay only {1} → prevent all
 * combat damage to and by that creature. Pay {2} → no further effect.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WintersChillEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WintersChillEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        WintersChillState state = gameData.wintersChill;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            List<UUID> targets = entry.getTargetIds();
            if (targets != null) {
                state.remainingTargetIds.addAll(targets);
            } else if (entry.getTargetId() != null) {
                state.remainingTargetIds.add(entry.getTargetId());
            }
            advance(gameData, sourceName);
            return;
        }

        if (state.chosenMode != null) {
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, sourceName, state.currentTargetId, mode);
            advance(gameData, sourceName);
            return;
        }

        advance(gameData, sourceName);
    }

    private void advance(GameData gameData, String sourceName) {
        WintersChillState state = gameData.wintersChill;
        while (true) {
            if (state.remainingTargetIds.isEmpty()) {
                gameData.rerunCurrentEffectAfterInteraction = false;
                state.reset();
                return;
            }

            UUID targetId = state.remainingTargetIds.pollFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }

            UUID controllerId = gameData.findControllerOf(targetId);
            state.currentTargetId = targetId;

            List<String> options = availableOptions(gameData, controllerId);
            if (options.size() == 1) {
                // Only "Pay nothing" — auto-apply destroy-at-end-of-combat.
                applyMode(gameData, sourceName, targetId, ChoiceContext.WintersChillPaymentChoice.PAY_NOTHING);
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName + " — for " + target.getCard().getName()
                    + ", may pay {1} or {2}.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.WintersChillPaymentChoice(controllerId, targetId, sourceName),
                    options, prompt));
            return;
        }
    }

    private void applyMode(GameData gameData, String sourceName, UUID targetId, String mode) {
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }
        UUID controllerId = gameData.findControllerOf(targetId);

        if (ChoiceContext.WintersChillPaymentChoice.PAY_TWO.equals(mode)) {
            if (!pay(gameData, controllerId, "{2}")) {
                // Can't pay — treat as pay nothing.
                scheduleDestroy(gameData, target);
                return;
            }
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "Pays {2} for ", target.getCard(), " (" + sourceName + ")."));
            return;
        }

        if (ChoiceContext.WintersChillPaymentChoice.PAY_ONE.equals(mode)) {
            if (!pay(gameData, controllerId, "{1}")) {
                scheduleDestroy(gameData, target);
                return;
            }
            gameData.creaturesWithCombatDamagePrevented.add(targetId);
            gameData.creaturesPreventedFromDealingCombatDamage.add(targetId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                    "Pays {1} for ", target.getCard(),
                    " — all combat damage to and by it this combat is prevented (" + sourceName + ")."));
            return;
        }

        // Pay nothing
        scheduleDestroy(gameData, target);
    }

    private void scheduleDestroy(GameData gameData, Permanent target) {
        gameData.queueDelayedAction(new DelayedPermanentAction(target.getId(),
                DelayedPermanentActionKind.DESTROY_AT_END_OF_COMBAT, false));
        gameBroadcastService.logAndBroadcast(gameData,
                GameLog.cardThen(target.getCard(), " will be destroyed at end of combat."));
    }

    private List<String> availableOptions(GameData gameData, UUID playerId) {
        List<String> options = new ArrayList<>();
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool != null && new ManaCost("{2}").canPay(pool)) {
            options.add(ChoiceContext.WintersChillPaymentChoice.PAY_TWO);
        }
        if (pool != null && new ManaCost("{1}").canPay(pool)) {
            options.add(ChoiceContext.WintersChillPaymentChoice.PAY_ONE);
        }
        options.add(ChoiceContext.WintersChillPaymentChoice.PAY_NOTHING);
        return options;
    }

    private boolean pay(GameData gameData, UUID playerId, String costString) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        ManaCost cost = new ManaCost(costString);
        if (pool == null || !cost.canPay(pool)) {
            return false;
        }
        cost.pay(pool);
        return true;
    }
}
