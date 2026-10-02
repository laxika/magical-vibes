package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Agent of Raffine's top-card duplicate and face-down exile. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTargetPlayerLibraryTopCardThenExileEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetPlayerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.getFirst();
        Card duplicate = topCard.createCardCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(duplicate.getId());
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.cardThen(duplicate, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));

        library.removeFirst();
        exileService.exileCardFaceDown(gameData, targetPlayerId, topCard, null, targetPlayerId);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(targetPlayerId)
                        + " exiles the top card of their library face down."));
    }
}
