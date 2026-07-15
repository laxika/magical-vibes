package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageToTargetCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageToTargetCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RedirectNextDamageToTargetCreatureEffect e = (RedirectNextDamageToTargetCreatureEffect) effect;
        UUID protectedPermanentId = entry.getSourcePermanentId();
        UUID redirectTargetId = entry.getTargetId();
        // Without the source creature or a legal redirect target, the ability does nothing.
        if (protectedPermanentId == null || redirectTargetId == null) return;
        Permanent redirectTarget = gameQueryService.findPermanentById(gameData, redirectTargetId);
        if (redirectTarget == null) return;

        // Any-source (null), amount-limited redirect shield protecting the ability's own creature.
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                protectedPermanentId, null, e.amount(), redirectTargetId));

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, protectedPermanentId);
        String protectedName = protectedPerm != null ? protectedPerm.getCard().getName() : "the creature";
        String logEntry = "The next " + e.amount() + " damage that would be dealt to " + protectedName
                + " this turn is dealt to " + redirectTarget.getCard().getName() + " instead.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - registered next-{}-damage redirect from {} to {}", gameData.id, e.amount(),
                protectedName, redirectTarget.getCard().getName());
    }
}
