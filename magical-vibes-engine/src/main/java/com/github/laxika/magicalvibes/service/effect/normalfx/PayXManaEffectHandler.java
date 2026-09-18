package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class PayXManaEffectHandler implements NormalEffectHandlerBean {

    private static final ManaCost GENERIC_X_COST = new ManaCost("{X}");

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.getOrDefault(controllerId, "Player");

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;
            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (chosenValue < 0 || !canPay(pool, chosenValue)) {
                beginChoice(gameData, entry);
                return;
            }

            if (chosenValue > 0) {
                pay(pool, chosenValue);
                gameLogService.append(gameData, GameLog.text(
                        playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            } else {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " pays no mana for " + cardName + "."));
            }
            entry.setEventValue(chosenValue);
            return;
        }

        if (maxPotentialX(gameData, controllerId) <= 0) {
            entry.setEventValue(0);
            return;
        }
        beginChoice(gameData, entry);
    }

    private void beginChoice(GameData gameData, StackEntry entry) {
        int maxValue = maxPotentialX(gameData, entry.getControllerId());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.XValueChoice(
                entry.getControllerId(), maxValue,
                "Pay any amount of mana for " + entry.getCard().getName() + ". Choose X (0 = pay no mana).",
                entry.getCard().getName(), true));
    }

    private int maxPotentialX(GameData gameData, java.util.UUID controllerId) {
        ManaPool pool = potentialManaService.buildVirtualManaPoolForSpecialAction(gameData, controllerId);
        int low = 0;
        int high = Math.max(0, pool.getTotalAllMana());
        while (low < high) {
            int candidate = low + (high - low + 1) / 2;
            if (canPay(pool, candidate)) {
                low = candidate;
            } else {
                high = candidate - 1;
            }
        }
        return low;
    }

    private static boolean canPay(ManaPool pool, int value) {
        return GENERIC_X_COST.canPay(pool, value, false, false, false, false, false,
                Set.of(), Set.of(), false, false, false, false, Set.of(), Set.of(), true);
    }

    private static void pay(ManaPool pool, int value) {
        GENERIC_X_COST.pay(pool, value, false, false, false, false, false,
                Set.of(), Set.of(), false, false, false, false, Set.of(), Set.of(), true);
    }
}
