package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaBoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaBoostTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final BoostTargetCreatureEffectHandler boostTargetCreatureEffectHandler;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaBoostTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            ManaCost cost = new ManaCost("{X}");
            if (!cost.canPay(pool, chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            cost.pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays X={} for {}", gameData.id, playerName, chosenValue, cardName);

            UUID targetId = entry.getTargetId();
            if (targetId != null) {
                var target = gameQueryService.findPermanentById(gameData, targetId);
                if (target != null) {
                    boostTargetCreatureEffectHandler.resolveForTarget(
                            gameData, entry, target, new BoostTargetCreatureEffect(chosenValue, 0));
                }
            }
            return;
        }

        ManaCost cost = new ManaCost("{X}");
        int maxX = cost.calculateMaxX(
                potentialManaService.buildVirtualManaPool(gameData, controllerId));
        if (maxX <= 0) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " has no mana to pay for " + cardName + "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X boost ability",
                    gameData.id, playerName, cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cardName);
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = new ManaCost("{X}").calculateMaxX(
                potentialManaService.buildVirtualManaPool(gameData, controllerId));
        String prompt = "You may pay {X} for " + cardName
                + ". Choose X (0 = don't pay). Target creature gets +X/+0.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }
}
