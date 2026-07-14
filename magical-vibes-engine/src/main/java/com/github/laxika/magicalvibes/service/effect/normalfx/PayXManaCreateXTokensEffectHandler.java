package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PayXManaCreateXTokensEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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
    private final GameBroadcastService gameBroadcastService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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

        // Re-entry after player chose X value
        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            if (chosenValue == 0) {
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " chooses X=0 for " + cardName + "'s ability.");
                log.info("Game {} - {} chooses X=0 for {}", gameData.id, playerName, cardName);
                return;
            }

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            new ManaCost("{0}").pay(pool, chosenValue);

            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " pays {" + chosenValue + "} for " + cardName + ".");
            log.info("Game {} - {} pays {} mana for {}", gameData.id, playerName, chosenValue, cardName);
            permanentControlSupport.applyCreateToken(
                    gameData, controllerId, e.token(), chosenValue, entry.getCard().getSetCode());
            return;
        }

        // First call: prompt for X value
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        int maxX = pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
        if (maxX <= 0) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " has no mana to pay for " + cardName + "'s ability.");
            log.info("Game {} - {} has no mana for {}'s pay-X token ability", gameData.id, playerName, cardName);
            return;
        }
        String prompt = "Pay {X} for " + cardName + "? Create X tokens.";
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(controllerId, maxX, prompt, cardName));
    }
}
