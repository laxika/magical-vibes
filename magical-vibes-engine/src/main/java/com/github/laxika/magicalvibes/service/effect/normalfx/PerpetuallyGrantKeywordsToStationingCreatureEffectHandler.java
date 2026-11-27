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

/** "Whenever a creature stations this Spacecraft, that creature perpetually gains [keywords]". */
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
        // "That creature": the stationing permanent while it is still on the battlefield; a card it became elsewhere is
        // a new object (CR 400.7).
        Permanent permanent = entry.getTriggeringPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTriggeringPermanentId());
        if (permanent == null) {
            return;
        }
        Card card = permanent.getOriginalCard() != null ? permanent.getOriginalCard() : permanent.getCard();

        Set<Keyword> grantable = EnumSet.noneOf(Keyword.class);
        for (var keyword : e.keywords()) {
            if (!gameQueryService.cantHaveOrGainKeyword(gameData, permanent, keyword)) {
                grantable.add(keyword);
            }
        }
        if (grantable.isEmpty()) {
            return;
        }

        gameData.grantPerpetualKeywords(card.getId(), grantable);
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
