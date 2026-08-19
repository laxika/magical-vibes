package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaCreateXTokensEffectHandler implements NormalEffectHandlerBean {

    private final PermanentControlSupport permanentControlSupport;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaCreateXTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaCreateXTokensEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameLogService.append(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            ManaCost cost = new ManaCost(e.manaCost());
            if (!cost.canPay(pool, chosenValue)) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay " + e.manaCost().replace("{X}", "{" + chosenValue + "}")
                                + " for " + cardName + " (tap mana sources, then choose X again)."));
                log.info("Game {} - {} cannot yet pay X={} for {} — re-prompting",
                        gameData.id, playerName, chosenValue, cardName);
                beginXPrompt(gameData, controllerId, cost, cardName, e.manaCost());
                return;
            }
            cost.pay(pool, chosenValue);

            gameLogService.append(gameData, GameLog.text(playerName + " pays "
                    + e.manaCost().replace("{X}", "{" + chosenValue + "}") + " for " + cardName + "."));
            log.info("Game {} - {} pays X={} for {}", gameData.id, playerName, chosenValue, cardName);
            entry.getCreatedPermanentIds().addAll(permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.token(), chosenValue, entry.getCard().getSetCode()));
            return;
        }

        ManaCost cost = new ManaCost(e.manaCost());
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, controllerId));
        if (maxX <= 0) {
            gameLogService.append(gameData, GameLog.text(playerName + " has no mana to pay for " + cardName + "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X token ability", gameData.id, playerName, cardName);
            return;
        }
        beginXPrompt(gameData, controllerId, cost, cardName, e.manaCost());
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, ManaCost cost, String cardName, String manaCost) {
        int maxX = cost.calculateMaxX(potentialManaService.buildVirtualManaPool(gameData, controllerId));
        String prompt = "You may pay " + manaCost + " for " + cardName
                + ". Choose X (0 = don't pay). Create X tokens.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName, true));
    }
}
