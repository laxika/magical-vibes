package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class ConjureCardIntoTopCardsOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoTopCardsOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardIntoTopCardsOfLibraryEffect conjure =
                (ConjureCardIntoTopCardsOfLibraryEffect) effect;
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        Card card = conjure.cardFactory().get();
        if (card == null) {
            return;
        }
        card.setOwnerId(entry.getControllerId());
        conjure.addedCastingOptions().forEach(card::addCastingOption);
        card.freeze();

        int insertionSlots = Math.min(conjure.count(), library.size() + 1);
        int insertionIndex = ThreadLocalRandom.current().nextInt(insertionSlots);
        library.add(insertionIndex, card);
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}
