package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicatesOfDyingCreatureIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Gyox's oil-counter-scaled duplicate conjure. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicatesOfDyingCreatureIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicatesOfDyingCreatureIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureDuplicatesOfDyingCreatureIntoLibraryEffect conjure =
                (ConjureDuplicatesOfDyingCreatureIntoLibraryEffect) effect;
        if (conjure.oilCounterCount() <= 0) {
            return;
        }

        Card dyingCard = entry.lastKnownPermanentCard(entry.getTriggeringPermanentId());
        if (dyingCard == null && conjure.dyingCardId() != null) {
            dyingCard = gameQueryService.findCardInGraveyardById(gameData, conjure.dyingCardId());
        }
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (dyingCard == null || library == null) {
            return;
        }

        for (int i = 0; i < conjure.oilCounterCount(); i++) {
            Card duplicate = dyingCard.createCardCopy();
            duplicate.setOwnerId(entry.getControllerId());
            duplicate.setToken(true);
            duplicate.setTokenCard(true);
            duplicate.freeze();
            PerpetualCardPowerToughnessSupport.remember(
                    gameData, duplicate, conjure.oilCounterCount(), conjure.oilCounterCount());
            library.add(duplicate);
        }

        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, entry.getControllerId(), conjure.oilCounterCount());
        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getControllerId());
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " conjures " + conjure.oilCounterCount()
                        + " duplicate" + (conjure.oilCounterCount() == 1 ? "" : "s")
                        + " into their library, then shuffles."));
    }
}
