package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Goblin Morale Sergeant's duplicate conjure. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "A duplicate of that creature": the triggering permanent where it is now, else as it last existed.
        Permanent enlisted = entry.getTriggeringPermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (enlisted == null) {
            enlisted = entry.getTriggeringPermanentSnapshot();
        }
        if (enlisted == null) {
            return;
        }
        Card source = enlisted.getCard();

        Card duplicate = source.createConjuredCopy();
        UUID controllerId = entry.getControllerId();
        duplicate.setOwnerId(controllerId);
        EnumSet<Keyword> keywords = duplicate.getKeywords() == null || duplicate.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(duplicate.getKeywords());
        keywords.add(Keyword.HASTE);
        duplicate.setKeywords(keywords);
        duplicate.freeze();

        PerpetualCardPowerToughnessSupport.remember(gameData, duplicate, 1, 0);
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null) {
            return;
        }
        int insertionBound = Math.min(4, library.size());
        int insertionIndex = ThreadLocalRandom.current().nextInt(insertionBound + 1);
        library.add(insertionIndex, duplicate);
        triggerCollectionService.checkConjureTriggers(gameData, controllerId, 1);
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into the top five cards of your library."));
    }
}
