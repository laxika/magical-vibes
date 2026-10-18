package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** Resolves a perpetual keyword grant on the source card. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantKeywordToSourceEffect) effect;
        if (entry.getSourcePermanentId() == null) {
            Card sourceCard = entry.getCard();
            if (sourceCard != null) {
                recordExtraInstanceIfAlreadyGranted(gameData, sourceCard, grant.keyword());
                gameData.perpetualCardKeywords
                        .computeIfAbsent(sourceCard.getId(), ignored -> EnumSet.noneOf(Keyword.class))
                        .add(grant.keyword());
            }
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        recordExtraInstanceIfAlreadyGranted(gameData, source.getCard(), grant.keyword());
        Card modifiedCard = source.getCard().createRuntimeCopy();
        EnumSet<Keyword> keywords = modifiedCard.getKeywords().isEmpty()
                ? EnumSet.noneOf(Keyword.class)
                : EnumSet.copyOf(modifiedCard.getKeywords());
        keywords.add(grant.keyword());
        modifiedCard.setKeywords(keywords);
        modifiedCard.freeze();
        source.exchangeCard(modifiedCard);
    }

    /**
     * A card that perpetually gains a keyword it already has gets another instance of it, which matters
     * for keywords whose instances trigger separately (CR 702.91b battle cry).
     */
    private static void recordExtraInstanceIfAlreadyGranted(GameData gameData, Card card, Keyword keyword) {
        if (card.getKeywords().contains(keyword)
                || gameData.perpetualCardKeywords.getOrDefault(card.getId(), Set.of()).contains(keyword)) {
            gameData.perpetualCardExtraKeywordInstances
                    .computeIfAbsent(card.getId(), ignored -> new ConcurrentHashMap<>())
                    .merge(keyword, 1, Integer::sum);
        }
    }
}
