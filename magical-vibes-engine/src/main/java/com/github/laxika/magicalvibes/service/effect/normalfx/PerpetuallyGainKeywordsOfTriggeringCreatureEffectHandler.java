package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGainKeywordsOfTriggeringCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PerpetuallyGainKeywordsOfTriggeringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGainKeywordsOfTriggeringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        // "This creature": the source permanent's own card while it is still that permanent (CR 400.7).
        var self = subjectResolutionService.sourceObjectCard(gameData, entry, effect);
        if (self == null || self.zone() != Zone.BATTLEFIELD) {
            return;
        }
        Card sourceCard = self.card();

        Permanent triggeringCreature = gameQueryService.findPermanentById(
                gameData, entry.getTriggeringPermanentId());
        Set<Keyword> keywords = EnumSet.noneOf(Keyword.class);
        if (triggeringCreature != null) {
            for (Keyword keyword : PerpetuallyGainKeywordsOfTriggeringCreatureEffect.SUPPORTED_KEYWORDS) {
                if (gameQueryService.hasKeyword(gameData, triggeringCreature, keyword)) {
                    keywords.add(keyword);
                }
            }
        } else if (entry.getTriggeringPermanentKeywordsAtLeave() != null) {
            for (Keyword keyword : PerpetuallyGainKeywordsOfTriggeringCreatureEffect.SUPPORTED_KEYWORDS) {
                if (entry.getTriggeringPermanentKeywordsAtLeave().contains(keyword)) {
                    keywords.add(keyword);
                }
            }
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        keywords.removeIf(keyword -> source != null
                && gameQueryService.cantHaveOrGainKeyword(gameData, source, keyword));
        if (keywords.isEmpty()) {
            return;
        }

        gameData.grantPerpetualKeywords(sourceCard.getId(), keywords);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " perpetually gains " + formatKeywords(keywords) + "."));
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.name().charAt(0)
                        + keyword.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((left, right) -> left + " and " + right)
                .orElse("");
    }
}
