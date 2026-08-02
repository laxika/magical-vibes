package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilLandsMillTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilLandsMillTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealUntilLandsMillTargetPlayerEffect e = (RevealUntilLandsMillTargetPlayerEffect) effect;

        for (UUID playerId : resolveRevealingPlayers(gameData, entry, e)) {
            revealAndMill(gameData, playerId, e.landCount());
        }
    }

    private void revealAndMill(GameData gameData, UUID targetPlayerId, int landCount) {
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Reveal cards from the top of the target player's library until landCount lands are revealed
        // (or the library empties).
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> revealedCards = new ArrayList<>();
        int landsRevealed = 0;

        while (deck != null && !deck.isEmpty() && landsRevealed < landCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.LAND)) {
                landsRevealed++;
            }
        }

        if (revealedCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(targetName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(targetName + " reveals " + revealedNames + "."));

        // All revealed cards go to the target player's graveyard.
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, targetPlayerId, card);
        }

        log.info("Game {} - {} reveals {} cards ({} lands) to their graveyard from Mind Funeral-style mill",
                gameData.id, targetName, revealedCards.size(), landsRevealed);
    }

    /**
     * TARGET_PLAYER reveals for the entry's player target; TARGET_PERMANENT_CONTROLLER reveals for
     * the controller of the targeted permanent (which must still be on the battlefield);
     * EACH_OPPONENT reveals for every player other than the effect's controller.
     */
    private List<UUID> resolveRevealingPlayers(GameData gameData, StackEntry entry, RevealUntilLandsMillTargetPlayerEffect effect) {
        if (effect.recipient() == MillRecipient.EACH_OPPONENT) {
            return gameData.orderedPlayerIds.stream()
                    .filter(playerId -> !playerId.equals(entry.getControllerId()))
                    .toList();
        }

        if (effect.recipient() != MillRecipient.TARGET_PERMANENT_CONTROLLER) {
            return entry.getTargetId() == null ? List.of() : List.of(entry.getTargetId());
        }

        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return List.of();
        }
        UUID controller = gameQueryService.findPermanentController(gameData, target.getId());
        return controller == null ? List.of() : List.of(controller);
    }
}
