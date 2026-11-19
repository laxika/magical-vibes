package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
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
    private final AmountEvaluationService amountEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilLandsMillTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealUntilLandsMillTargetPlayerEffect e = (RevealUntilLandsMillTargetPlayerEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int landCount = amountEvaluationService.evaluate(gameData, e.landCount(),
                AmountContext.forStackEntry(entry, source));

        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            revealAndMill(gameData, playerId, landCount);
        }
    }

    private void revealAndMill(GameData gameData, UUID targetPlayerId, int landCount) {
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Reveal cards from the top of the target player's library until landCount lands are revealed
        // (or the library empties).
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> revealedCards = new ArrayList<>();
        int landsRevealed = 0;

        while (deck != null && revealedCards.size() < deck.size() && landsRevealed < landCount) {
            Card card = deck.get(revealedCards.size());
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
        graveyardService.resolvePutTopCardsIntoGraveyard(gameData, targetPlayerId, revealedCards.size());

        log.info("Game {} - {} reveals {} cards ({} lands) to their graveyard from Mind Funeral-style mill",
                gameData.id, targetName, revealedCards.size(), landsRevealed);
    }
}
