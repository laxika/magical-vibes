package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantKeywordEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordEffect) effect;
        if (grant.scope() == GrantScope.OWN_CREATURES) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            int count = 0;
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                if (grant.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    continue;
                }
                permanent.getGrantedKeywords().addAll(grant.keywords());
                count++;
            }

            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = entry.getCard().getName() + " gives " + keywordNames + " to " + count + " creature(s) until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} own creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count);
            return;
        }

        if (grant.scope() == GrantScope.ALL_CREATURES) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                    .withSourceControllerId(entry.getControllerId());
            final int[] count = {0};
            gameData.forEachPermanent((playerId, permanent) -> {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    return;
                }
                if (grant.filter() != null
                        && !gameQueryService.matchesPermanentPredicate(permanent, grant.filter(), filterContext)) {
                    return;
                }
                permanent.getGrantedKeywords().addAll(grant.keywords());
                count[0]++;
            });

            String keywordNames = formatKeywords(grant.keywords());
            String logEntry = entry.getCard().getName() + " gives " + keywordNames + " to " + count[0] + " creature(s) until end of turn.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} grants {} to {} creature(s)", gameData.id, entry.getCard().getName(), grant.keywords(), count[0]);
            return;
        }

        UUID targetId = switch (grant.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            case TARGET -> entry.getTargetId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        target.getGrantedKeywords().addAll(grant.keywords());
        String keywordNames = formatKeywords(grant.keywords());
        String logEntry = target.getCard().getName() + " gains " + keywordNames + " until end of turn.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gains {} ({})", gameData.id, target.getCard().getName(), grant.keywords(), grant.scope());
    }

    private String formatKeywords(Set<Keyword> keywords) {
        return keywords.stream()
                .map(k -> k.name().charAt(0) + k.name().substring(1).toLowerCase().replace('_', ' '))
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }
}
