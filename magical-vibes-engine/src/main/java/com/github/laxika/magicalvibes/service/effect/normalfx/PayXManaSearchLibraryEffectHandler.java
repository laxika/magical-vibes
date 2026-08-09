package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ManaValueBound;
import com.github.laxika.magicalvibes.model.effect.PayXManaSearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.cast.PotentialManaService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PayXManaSearchLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PotentialManaService potentialManaService;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PayXManaSearchLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PayXManaSearchLibraryEffect) effect;
        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();
        String playerName = gameData.playerIdToName.get(controllerId);

        if (gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (!accepted) {
                return;
            }
            beginXPrompt(gameData, controllerId, cardName);
            return;
        }

        if (gameData.chosenXValue != null) {
            int chosenValue = gameData.chosenXValue;
            gameData.chosenXValue = null;

            ManaPool pool = gameData.playerManaPools.get(controllerId);
            if (payableFromPool(pool) < chosenValue) {
                gameLogService.append(gameData, GameLog.text(
                        playerName + " can't pay {" + chosenValue + "} for " + cardName
                                + " (tap mana sources, then choose X again)."));
                beginXPrompt(gameData, controllerId, cardName);
                return;
            }

            new ManaCost("{0}").pay(pool, chosenValue);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " pays {" + chosenValue + "} for " + cardName + "."));

            SearchLibraryEffect search = new SearchLibraryEffect(
                    e.filter(),
                    LibrarySearchDestination.BATTLEFIELD,
                    new ManaValueBound(new Fixed(chosenValue), false, 0));
            searchLibraryEffectHandler.resolve(gameData, entry, search);
            return;
        }

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(e),
                cardName + " - Pay {X} to search your library for a matching card?",
                entry.getTargetId(),
                null,
                entry.getSourcePermanentId()
        ));
    }

    private void beginXPrompt(GameData gameData, UUID controllerId, String cardName) {
        int maxX = maxPotentialX(gameData, controllerId);
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.XValueChoice(
                        controllerId,
                        maxX,
                        "Choose X to pay for " + cardName + "'s ability.",
                        cardName,
                        true));
    }

    private int maxPotentialX(GameData gameData, UUID controllerId) {
        int untappedSources = potentialManaService.buildVirtualManaPool(gameData, controllerId).getTotal()
                - gameData.playerManaPools.get(controllerId).getTotal();
        return Math.max(0, payableFromPool(gameData.playerManaPools.get(controllerId)) + untappedSources);
    }

    private static int payableFromPool(ManaPool pool) {
        return pool.getTotal() + pool.getArtifactOnlyColorless() + pool.getMyrOnlyColorless();
    }
}
