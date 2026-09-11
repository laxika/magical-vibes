package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaMoveCountersFromSourceToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/**
 * Resolves the payment and reflexive-target portion of Tester of the Tangential's ability.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaMoveCountersFromSourceToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PotentialManaService potentialManaService;

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

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " declines to pay for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

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

        if (maxPotentialX(gameData, controllerId) <= 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no mana to pay for " + cardName + "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X ability", gameData.id, playerName, cardName);
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
        UUID sourcePermanentId = entry.getSourcePermanentId();
        List<UUID> validTargets = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (!permanent.getId().equals(sourcePermanentId)
                        && gameQueryService.isCreature(gameData, permanent)) {
                    validTargets.add(permanent.getId());
                }
            }
        }

        if (validTargets.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s reflexive ability has no legal target."));
            return;
        }

        List<CardEffect> moveEffects = IntStream.range(0, counterCount)
                .mapToObj(ignored -> (CardEffect) new MoveCounterFromSourceToTargetCreatureEffect(
                        CounterType.PLUS_ONE_PLUS_ONE))
                .toList();
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), moveEffects, sourcePermanentId, null));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validTargets,
                entry.getCard().getName() + "'s reflexive ability - Choose another target creature.");
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
