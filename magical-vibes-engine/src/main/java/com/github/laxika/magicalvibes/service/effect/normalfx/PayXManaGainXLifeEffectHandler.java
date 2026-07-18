package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaGainXLifeEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayXManaGainXLifeEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaGainXLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // Re-entry after player chose X value
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;
            String cardName = entry.getCard().getName();
            String playerName = gameData.playerIdToName.get(controllerId);

            if (chosenValue == 0) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " chooses X=0 for " + cardName + "'s ability."));
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            new ManaCost("{0}").pay(pool, chosenValue);

            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " pays {" + chosenValue + "} for " + cardName + "."));
            log.info("Game {} - {} pays {} mana for {}", gameData.id, playerName, chosenValue, cardName);
            lifeSupport.applyGainLife(gameData, controllerId, chosenValue, cardName);
            return;
        }

        // First call: prompt for X value
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int maxX = pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
        if (maxX <= 0) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(playerName + " has no mana to pay for ", entry.getCard(), "'s ability."));
            log.info("Game {} - {} has no mana for {}'s pay-X ability", gameData.id,
                    gameData.playerIdToName.get(controllerId), entry.getCard().getName());
            return;
        }
        String prompt = "Pay {X} for " + entry.getCard().getName() + "? You gain X life.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, entry.getCard().getName()));
    }
}
