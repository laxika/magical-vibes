package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

/** Records perpetual keyword grants on the card that caused the surrounding trigger. */
@Component
public class PerpetuallyGrantKeywordsToTriggeringCardEffectHandler
        implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

        var grant = (PerpetuallyGrantKeywordsToTriggeringCardEffect) effect;
        // A grant newer than an earlier perpetual loss of the keyword restores it; a keyword the card
        // already has gains another instance, which matters for keywords that trigger separately.
        java.util.Set<Keyword> removed = gameData.perpetualCardRemovedKeywords.get(cardId);
        for (Keyword keyword : grant.keywords()) {
            boolean lost = removed != null && removed.remove(keyword);
            if (!lost && gameData.perpetualCardKeywords.getOrDefault(cardId, java.util.Set.of()).contains(keyword)) {
                gameData.perpetualCardExtraKeywordInstances
                        .computeIfAbsent(cardId, ignored -> new java.util.concurrent.ConcurrentHashMap<>())
                        .merge(keyword, 1, Integer::sum);
            }
        }
        gameData.perpetualCardKeywords
                .computeIfAbsent(cardId, ignored -> EnumSet.noneOf(Keyword.class))
                .addAll(grant.keywords());
    }
}
