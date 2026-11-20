package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfOwnLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Each player the effect names exiles the top card of their library, in APNAP order. When the effect tracks the card
 * with its source, the card is linked to that exile ability, so it is not treated as the source's imprint: a permanent
 * that copies the source (Dermotaxi copying Rona) must not borrow it as the imprinted card. Records the
 * {@link StepKind#EXILE} step (the cards and their owners, or nothing on empty libraries).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfOwnLibraryEffectHandler implements NormalEffectHandlerBean {

    private static final String EXILE_ABILITY_LINK = "exile-top-card-of-own-library";

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfOwnLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTopCardOfOwnLibraryEffect) effect;
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            exileTopCard(gameData, entry, e, playerId);
        }
    }

    private void exileTopCard(GameData gameData, StackEntry entry, ExileTopCardOfOwnLibraryEffect e, UUID playerId) {
        List<Card> deck = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);

        if (deck == null || deck.isEmpty()) {
            if (e.trackWithSource() && entry.getSourcePermanentId() != null) {
                gameData.setImprintedCard(entry.getCard(), null);
            }
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty — nothing to exile."));
            return;
        }

        Card topCard = deck.removeFirst();

        if (e.trackWithSource() && entry.getSourcePermanentId() != null) {
            exileService.exileCard(gameData, playerId, topCard, entry.getSourcePermanentId());
            synchronized (gameData.exiledCards) {
                gameData.exiledCards.replaceAll(exiled -> exiled.card().getId().equals(topCard.getId())
                        ? exiled.withAbilityLink(EXILE_ABILITY_LINK) : exiled);
            }
            gameData.setImprintedCard(entry.getCard(), topCard);
        } else {
            exileService.exileCard(gameData, playerId, topCard);
        }

        entry.addToStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(topCard.getId()), List.of(playerId)));
        gameLogService.append(gameData, GameLog.textCardText(playerName + " exiles ", topCard,
                " from the top of their library."));
        log.info("Game {} - {} exiles {} from library top", gameData.id, playerName, topCard.getName());
    }
}
