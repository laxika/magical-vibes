package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCreatureFromOpponentLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Clone Crafter's random creature duplicate without removing the library card. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCreatureFromOpponentLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCreatureFromOpponentLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID opponentId = gameQueryService.getOpponentId(gameData, entry.getControllerId());
        List<Card> candidates = gameData.playerDecks.getOrDefault(opponentId, List.of()).stream()
                .filter(card -> !card.isToken())
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card copy = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).createCardCopy();
        copy.setOwnerId(entry.getControllerId());
        copy.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(copy.getId());
        gameData.addCardToHand(entry.getControllerId(), copy);
        gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
    }
}
