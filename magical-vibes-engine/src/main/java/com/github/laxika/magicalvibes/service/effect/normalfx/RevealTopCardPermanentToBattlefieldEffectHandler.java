package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Each player the effect names reveals the top card of their library; the permanent cards among them enter the
 * battlefield together under their owners' control through the entry batch, the others stay on top.
 */
@Component
@RequiredArgsConstructor
public class RevealTopCardPermanentToBattlefieldEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardPermanentToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var reveal = (RevealTopCardPermanentToBattlefieldEffect) effect;
        String sourceName = entry.getCard().getName();
        List<BattlefieldEntryCard> entering = new ArrayList<>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, reveal.players())) {
            List<Card> library = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (library == null || library.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty (" + sourceName + ")."));
                continue;
            }

            Card topCard = library.getFirst();
            gameLogService.append(gameData, GameLog.builder()
                    .text(playerName + " reveals ")
                    .card(topCard)
                    .text(" from the top of their library (" + sourceName + ").")
                    .build());

            if (!isPermanentCard(topCard)) {
                gameLogService.append(gameData, GameLog.builder()
                        .card(topCard)
                        .text(" remains on top of " + playerName + "'s library (" + sourceName + ").")
                        .build());
                continue;
            }
            entering.add(new BattlefieldEntryCard(playerId, playerId, topCard, Zone.LIBRARY, null));
        }
        if (!entering.isEmpty()) {
            battlefieldEntryBatchSupport.begin(gameData, entering);
        }
    }

    private boolean isPermanentCard(Card card) {
        if (card.getType() != null && card.getType().isPermanentType()
                && card.getType() != CardType.KINDRED) {
            return true;
        }
        return card.getAdditionalTypes().stream()
                .anyMatch(type -> type.isPermanentType() && type != CardType.KINDRED);
    }
}
