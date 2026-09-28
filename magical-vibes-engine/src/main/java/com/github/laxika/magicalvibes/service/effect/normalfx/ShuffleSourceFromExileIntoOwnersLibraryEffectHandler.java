package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleSourceFromExileIntoOwnersLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Moves the resolving graveyard ability's exiled source card into its owner's library. */
@Component
@RequiredArgsConstructor
public class ShuffleSourceFromExileIntoOwnersLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleSourceFromExileIntoOwnersLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getPhysicalCard();
        if (sourceCard == null || !gameData.removeFromExile(sourceCard.getId())) {
            return;
        }

        gameData.playerDecks.get(entry.getOwnerId()).add(sourceCard);
        triggerCollectionService.checkCardsPutIntoLibraryTriggers(
                gameData, entry.getOwnerId(), 1);
        LibraryShuffleHelper.shuffleLibrary(gameData, entry.getOwnerId());
        gameLogService.append(gameData,
                GameLog.textCardText(entry.getDescription() + " shuffles ", sourceCard,
                        " into its owner's library."));
    }
}
