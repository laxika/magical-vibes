package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class SeekEffectHandler implements NormalEffectHandlerBean {

    private final PredicateEvaluationService predicateEvaluationService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        SeekEffect seek = (SeekEffect) effect;
        List<Card> matching = library.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(
                        card, seek.filter(), null, gameData, playerId))
                .toList();
        if (matching.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
            return;
        }

        Card sought = matching.get(ThreadLocalRandom.current().nextInt(matching.size()));
        library.remove(sought);
        gameData.addCardToHand(playerId, sought);
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
        triggerCollectionService.checkSeekTriggers(gameData, playerId, List.of(sought));

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " seeks ", sought, " into their hand."));
    }
}
