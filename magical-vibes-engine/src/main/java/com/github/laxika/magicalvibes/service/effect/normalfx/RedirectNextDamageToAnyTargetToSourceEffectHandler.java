package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToAnyTargetToSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageToAnyTargetToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageToAnyTargetToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RedirectNextDamageToAnyTargetToSourceEffect) effect;
        UUID redirectTargetId = entry.getSourcePermanentId();
        UUID protectedId = entry.getTargetId();
        // Without the source permanent (the redirect destination) or a legal target, nothing happens.
        if (protectedId == null || redirectTargetId == null || e.amount() <= 0) return;
        Permanent source = gameQueryService.findPermanentById(gameData, redirectTargetId);
        if (source == null) return;

        if (gameData.playerIds.contains(protectedId)) {
            gameData.playerNextDamageRedirectShields.add(
                    new PlayerNextDamageRedirectShield(protectedId, e.amount(), redirectTargetId));
            gameLogService.append(gameData, GameLog.builder()
                    .text("The next " + e.amount() + " damage that would be dealt this turn to "
                            + gameData.playerIdToName.get(protectedId) + " is dealt to ")
                    .card(source.getCard())
                    .text(" instead.")
                    .build());
            return;
        }

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, protectedId);
        if (protectedPerm == null) return;

        // Any-source (null), amount-limited redirect shield protecting the targeted permanent.
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                protectedId, null, e.amount(), redirectTargetId));
        gameLogService.append(gameData, GameLog.builder()
                .text("The next " + e.amount() + " damage that would be dealt this turn to ")
                .card(protectedPerm.getCard())
                .text(" is dealt to ")
                .card(source.getCard())
                .text(" instead.")
                .build());
        log.info("Game {} - registered next-{}-damage redirect to {}", gameData.id, e.amount(),
                source.getCard().getName());
    }
}
