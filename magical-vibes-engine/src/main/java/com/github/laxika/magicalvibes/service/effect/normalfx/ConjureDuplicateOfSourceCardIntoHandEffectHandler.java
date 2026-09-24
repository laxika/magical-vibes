package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfSourceCardIntoHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfSourceCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfSourceCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        String setCode = sourceCard.getSetCode() != null
                ? sourceCard.getSetCode() : ((ConjureDuplicateOfSourceCardIntoHandEffect) effect).fallbackSetCode();
        String collectorNumber = sourceCard.getCollectorNumber() != null
                ? sourceCard.getCollectorNumber()
                : ((ConjureDuplicateOfSourceCardIntoHandEffect) effect).fallbackCollectorNumber();
        CardSet set = CardSet.findByCode(setCode);
        if (set == null) {
            throw new IllegalArgumentException("Unknown source card set code: " + setCode);
        }

        Card duplicate = cardCatalog.findByCollectorNumber(set, collectorNumber).createCard();
        Set<Keyword> removedKeywords = ((ConjureDuplicateOfSourceCardIntoHandEffect) effect).removedKeywords();
        if (!removedKeywords.isEmpty()) {
            EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
            keywords.addAll(duplicate.getKeywords());
            keywords.removeAll(removedKeywords);
            duplicate.setKeywords(Set.copyOf(keywords));
        }
        gameData.addCardToHand(entry.getControllerId(), duplicate);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " conjures ", duplicate,
                " into their hand."));
    }
}
