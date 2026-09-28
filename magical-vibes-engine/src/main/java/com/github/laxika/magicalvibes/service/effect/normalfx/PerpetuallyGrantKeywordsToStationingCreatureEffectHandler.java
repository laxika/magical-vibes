package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantKeywordsToStationingCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PerpetuallyGrantKeywordsToStationingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantKeywordsToStationingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PerpetuallyGrantKeywordsToStationingCreatureEffect) effect;
        Card card = gameQueryService.findCardById(gameData, entry.getTargetId());
        if (card == null) {
            return;
        }

        Set<Keyword> grantable = EnumSet.noneOf(Keyword.class);
        Permanent permanent = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        for (var keyword : e.keywords()) {
            if (permanent == null || !gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword)) {
                grantable.add(keyword);
            }
        }
        if (grantable.isEmpty()) {
            return;
        }

        gameData.perpetualCardKeywords
                .computeIfAbsent(card.getId(), ignored -> EnumSet.noneOf(Keyword.class))
                .addAll(grantable);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " perpetually gains " + formatKeywords(grantable) + "."));
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(keyword -> keyword.name().charAt(0)
                        + keyword.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((left, right) -> left + " and " + right)
                .orElse("");
    }
}
