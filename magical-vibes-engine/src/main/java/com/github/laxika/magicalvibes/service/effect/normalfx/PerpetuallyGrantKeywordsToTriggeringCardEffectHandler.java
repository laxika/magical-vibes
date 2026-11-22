package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

/** Records perpetual keyword grants on the card that caused the surrounding trigger. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordsToTriggeringCardEffectHandler
        implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantKeywordsToTriggeringCardEffect) effect;
        if (grant.cards() != null) {
            // Kept by the physical card's id, which a permanent that becomes a copy still has.
            for (Card card : subjectResolutionService.cards(gameData, entry, grant, grant.cards())) {
                java.util.Set<Keyword> granted = EnumSet.noneOf(Keyword.class);
                granted.addAll(gameData.perpetualKeywords.getOrDefault(card.getId(), java.util.Set.of()));
                granted.addAll(grant.keywords());
                gameData.perpetualKeywords.put(card.getId(), java.util.Set.copyOf(granted));
            }
            return;
        }
        UUID cardId = entry.getTriggeringCardId();
        if (cardId == null) {
            return;
        }

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
