package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectNextDamageToSelfToOwnerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectNextDamageToSelfToOwnerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RedirectNextDamageToSelfToOwnerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RedirectNextDamageToSelfToOwnerEffect e = (RedirectNextDamageToSelfToOwnerEffect) effect;
        UUID protectedPermanentId = entry.getSourcePermanentId();
        // The ability may only be activated by the creature's owner (controller), so the redirected
        // damage goes to the ability's controller.
        UUID ownerId = entry.getControllerId();
        // Without the source creature or its owner, the ability does nothing.
        if (protectedPermanentId == null || ownerId == null) return;

        // Amount-limited (next e.amount()), any-source (null) redirect shield protecting the ability's
        // own creature, destined for its owner (a player).
        gameData.creatureDamageRedirectShields.add(new CreatureDamageRedirectShield(
                protectedPermanentId, null, e.amount(), ownerId));

        Permanent protectedPerm = gameQueryService.findPermanentById(gameData, protectedPermanentId);
        String protectedName = protectedPerm != null ? protectedPerm.getCard().getName() : "the creature";
        String ownerName = gameData.playerIdToName.get(ownerId);
        String logEntry = "The next " + e.amount() + " damage that would be dealt to " + protectedName
                + " this turn is dealt to " + ownerName + " instead.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - registered next-{}-damage redirect from {} to its owner {}", gameData.id,
                e.amount(), protectedName, ownerName);
    }
}
