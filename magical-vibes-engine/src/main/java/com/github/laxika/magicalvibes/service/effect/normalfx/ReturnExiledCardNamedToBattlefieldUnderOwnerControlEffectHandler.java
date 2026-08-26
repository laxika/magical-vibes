package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a return-from-exile effect that identifies the card by name. */
@Component
@RequiredArgsConstructor
public class ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String cardName = ((ReturnExiledCardNamedToBattlefieldUnderOwnerControlEffect) effect).cardName();
        List<ExiledCardEntry> matching = matchingEntries(gameData, cardName);
        if (matching.isEmpty()) {
            return;
        }

        UUID controllerId = entry.getControllerId();
        if (matching.size() == 1) {
            returnToBattlefield(gameData, matching.getFirst());
            return;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ExiledCardChoice(
                controllerId,
                matching.stream().map(exiled -> exiled.card().getId()).toList(),
                cardName));
    }

    public List<ExiledCardEntry> matchingEntries(GameData gameData, String cardName) {
        synchronized (gameData.exiledCards) {
            return gameData.exiledCards.stream()
                    .filter(exiled -> !exiled.faceDown() && cardName.equals(exiled.card().getName()))
                    .toList();
        }
    }

    public ExiledCardEntry findMatchingEntry(GameData gameData, UUID cardId, String cardName) {
        synchronized (gameData.exiledCards) {
            return gameData.exiledCards.stream()
                    .filter(exiled -> exiled.card().getId().equals(cardId)
                            && !exiled.faceDown()
                            && cardName.equals(exiled.card().getName()))
                    .findFirst()
                    .orElse(null);
        }
    }

    public void returnToBattlefield(GameData gameData, ExiledCardEntry exiled) {
        if (!gameData.removeFromExile(exiled.card().getId())) {
            return;
        }

        Card card = exiled.card();
        UUID ownerId = exiled.ownerId();
        battlefieldEntryService.putPermanentOntoBattlefield(gameData, ownerId, new Permanent(card));
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(ownerId) + " returns ", card,
                " from exile to the battlefield."));
        battlefieldEntryService.handleCreatureEnteredBattlefield(gameData, ownerId, card, null, false);
    }
}
