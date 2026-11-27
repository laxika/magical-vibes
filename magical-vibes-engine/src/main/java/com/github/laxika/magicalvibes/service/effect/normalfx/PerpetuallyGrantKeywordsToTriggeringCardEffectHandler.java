package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToTriggeringCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Records perpetual keyword grants on the card that caused the surrounding trigger, or on the cards a subject names,
 * kept by the physical card's id (which a permanent that becomes a copy still has). A grant to the cards a subject
 * names is logged as {@link PerpetualChangeLog} says.
 */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordsToTriggeringCardEffectHandler
        implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToTriggeringCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (PerpetuallyGrantKeywordsToTriggeringCardEffect) effect;
        var located = subjectResolutionService.locatedCards(gameData, entry, grant, grant.cards());
        located.forEach(card -> grant(gameData, card.card(), grant.keywords()));
        PerpetualChangeLog.log(gameLogService, subjectResolutionService, gameData, entry, grant, grant.cards(),
                located, " perpetually gains " + grant.keywords().stream()
                        .map(keyword -> keyword.name().toLowerCase(Locale.ROOT).replace('_', ' '))
                        .sorted().collect(Collectors.joining(" and ")) + ".");
    }

    /**
     * A grant newer than an earlier perpetual loss of the keyword restores it; a keyword the card already has, printed
     * or gained, gains another instance, which matters for keywords that trigger separately (CR 113.2c, 702.91b).
     */
    private static void grant(GameData gameData, Card card, Set<Keyword> keywords) {
        UUID cardId = card.getId();
        Set<Keyword> removed = gameData.perpetualCardRemovedKeywords.get(cardId);
        for (Keyword keyword : keywords) {
            boolean lost = removed != null && removed.remove(keyword);
            if (!lost && (card.getKeywords().contains(keyword)
                    || gameData.perpetualCardKeywords.getOrDefault(cardId, Set.of()).contains(keyword))) {
                gameData.perpetualCardExtraKeywordInstances
                        .computeIfAbsent(cardId, ignored -> new ConcurrentHashMap<>())
                        .merge(keyword, 1, Integer::sum);
            }
        }
        gameData.grantPerpetualKeywords(cardId, keywords);
    }
}
