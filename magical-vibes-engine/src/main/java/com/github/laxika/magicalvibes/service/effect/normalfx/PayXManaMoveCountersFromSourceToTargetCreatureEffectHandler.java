package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.service.effect.ReflexiveAbilitySupport;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaMoveCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves the payment and reflexive-target portion of Tester of the Tangential's ability.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaMoveCountersFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggeredAbilityStackService triggeredAbilityStackService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    private static final CardEffect MOVE_X_COUNTERS =
            MoveCounterFromSourceToTargetCreatureEffect.ontoAnotherCreature(CounterType.PLUS_ONE_PLUS_ONE,
                    new XValue());

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaMoveCountersFromSourceToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);
        ManaCost cost = new ManaCost("{X}");

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (!cost.canPay(pool, chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            cost.pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays X={} for {}", gameData.id, playerName, chosenValue, cardName);
            queueReflexiveTargetChoice(gameData, entry, chosenValue);
            return;
        }

        beginXPrompt(gameData, controllerId, cardName);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        String prompt = "Pay {X} for " + cardName + "? Move X +1/+1 counters onto another target creature.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }

    private void queueReflexiveTargetChoice(GameData gameData, StackEntry entry, int counterCount) {
        // "Move X +1/+1 counters": the ability records what the resolving entry was about, with the X paid as its X.
        List<CardEffect> moveEffects = List.of(MOVE_X_COUNTERS);
        StackEntry record = ReflexiveAbilitySupport.record(gameData, entry, moveEffects);
        record.setXValue(counterCount);
        // "When you do, move X +1/+1 counters ... onto another target creature" (CR 603.12): its target is chosen
        // as it goes on the stack.
        triggeredAbilityStackService.putMade(gameData, record);
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - pool.getTotal();
        return payableFromPool(pool) + untappedSources;
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }
}
