package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect conjure =
                (ConjureDuplicateOfEnlistedCreatureIntoTopFiveEffect) effect;
        Card source = conjure.creatureCard();
        if (source == null) {
            return;
        }

        Card duplicate = source.createRuntimeCopyWithNewId();
        UUID controllerId = entry.getControllerId();
        duplicate.setOwnerId(controllerId);
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
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
        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into the top five cards of your library."));
    }
}
