package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetCreatureForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Has the target player create a token copy of the target creature through the shared token-copy pipeline, so
 * that player orders the token creation replacements they control (CR 616.1).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfTargetCreatureForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCopierService permanentCopierService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfTargetCreatureForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetIds() == null || entry.getTargetIds().size() < 2) {
            return;
        }

        UUID tokenControllerId = entry.getTargetIds().getFirst();
        UUID creatureTargetId = entry.getTargetIds().get(1);
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, creatureTargetId);
        if (targetPermanent == null) {
            log.info("Game {} - Target creature no longer on battlefield, no token created", gameData.id);
            return;
        }

        tokenCopySupport.createTokenCopies(gameData, entry,
                List.of(permanentCopierService.copiableCard(targetPermanent)), null, tokenControllerId,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
