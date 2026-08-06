package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceNextCombatDamageToOpponentRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextCombatDamageToOpponentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextCombatDamageToOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextCombatDamageToOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourceId = entry.getSourcePermanentId();
        UUID destinationId = entry.getTargetId();
        if (sourceId == null || destinationId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
        Permanent destination = gameQueryService.findPermanentById(gameData, destinationId);
        // A source that has left the battlefield can no longer deal combat damage, and a
        // destination that has left cannot receive the redirected damage.
        if (source == null || destination == null) {
            return;
        }

        gameData.sourceNextCombatDamageToOpponentRedirectShields.add(
                new SourceNextCombatDamageToOpponentRedirectShield(sourceId, destinationId));

        gameLogService.append(gameData, GameLog.builder()
                .text("The next time ")
                .card(source.getCard())
                .text(" would deal combat damage to an opponent this turn, it deals that damage to ")
                .card(destination.getCard())
                .text(" instead.")
                .build());
        log.info("Game {} - registered next-combat-damage redirect from {} to {}", gameData.id,
                source.getCard().getName(), destination.getCard().getName());
    }
}
