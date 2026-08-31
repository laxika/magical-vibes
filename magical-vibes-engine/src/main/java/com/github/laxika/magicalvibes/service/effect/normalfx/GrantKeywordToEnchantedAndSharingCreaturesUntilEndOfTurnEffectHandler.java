package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantKeywordToEnchantedAndSharingCreaturesUntilEndOfTurnEffect) effect;
        Permanent enchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (enchanted == null || !gameQueryService.isCreature(gameData, enchanted)) {
            return;
        }

        int[] count = {0};
        gameData.forEachPermanent((ignored, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)
                    || (!permanent.getId().equals(enchanted.getId())
                    && !gameQueryService.shareCreatureType(gameData, enchanted, permanent))) {
                return;
            }

            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), null, entry.getControllerId(),
                    new GrantKeywordEffect(grant.keywords(), GrantScope.TARGET),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
            count[0]++;
        });

        String keywords = grant.keywords().stream()
                .map(Keyword::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" and "));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" gives " + keywords + " to " + count[0] + " creature(s) until end of turn.")
                .build());
    }
}
