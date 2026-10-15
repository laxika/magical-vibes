package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureRandomCardFromTargetPlayerLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a random duplicate from a target player's library. */
@Component
@RequiredArgsConstructor
public class ConjureRandomCardFromTargetPlayerLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureRandomCardFromTargetPlayerLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        List<Card> candidates = gameData.playerDecks.getOrDefault(targetPlayerId, List.of()).stream()
                .filter(card -> !card.isToken())
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card copy = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size())).createConjuredCopy();
        copy.setOwnerId(entry.getControllerId());
        copy.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(copy.getId());
        gameData.addCardToHand(entry.getControllerId(), copy);
        gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}
