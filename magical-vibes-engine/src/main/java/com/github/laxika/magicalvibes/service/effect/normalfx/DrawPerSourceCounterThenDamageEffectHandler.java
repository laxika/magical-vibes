package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawPerSourceCounterThenDamageEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link DrawPerSourceCounterThenDamageEffect}: the draw-step player draws one extra card
 * per counter of the effect's type on the source permanent, then the source deals that much damage
 * to them. The counter count is read once, before the draws, so both halves use the same number.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrawPerSourceCounterThenDamageEffectHandler implements NormalEffectHandlerBean {

    private final DrawService drawService;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawPerSourceCounterThenDamageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawPerSourceCounterThenDamageEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        if (source == null) {
            return;
        }

        int count = source.getCounterCount(e.counterType());
        if (count <= 0) {
            return;
        }

        UUID playerId = entry.getTargetId();
        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }

        damageSupport.dealDamageToPlayer(gameData, entry, playerId, count);
        log.info("Game {} - {} made player {} draw {} card(s) and dealt {} damage",
                gameData.id, entry.getCard().getName(), playerId, count, count);
    }
}
