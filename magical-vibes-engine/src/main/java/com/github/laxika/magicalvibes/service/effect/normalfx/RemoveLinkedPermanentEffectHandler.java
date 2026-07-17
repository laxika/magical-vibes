package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Removes the permanent linked to the trigger source (Dance of Many's mutual bond). The trigger
 * collector bakes the linked permanent's id into the stack entry's {@code targetId}; here we clear the
 * partner's back-reference first so removing it does not re-trigger the reciprocal removal, then exile
 * or sacrifice it per {@link RemoveLinkedPermanentEffect.Mode}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RemoveLinkedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveLinkedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveLinkedPermanentEffect) effect;

        Permanent linked = e.linkedPermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, e.linkedPermanentId());
        if (linked == null) {
            return;
        }

        // Break the bond before removal so the linked permanent's own leaves-battlefield trigger becomes
        // a no-op (avoids the reciprocal exile ↔ sacrifice bouncing back).
        linked.setChosenPermanentId(null);

        String name = linked.getCard().getName();
        switch (e.mode()) {
            case EXILE -> {
                permanentRemovalService.removePermanentToExile(gameData, linked);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(name + " is exiled."));
            }
            case SACRIFICE -> {
                permanentRemovalService.removePermanentToGraveyard(gameData, linked);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(name + " is sacrificed."));
            }
        }
        log.info("Game {} - {} removed ({}) via linked leaves-battlefield trigger",
                gameData.id, name, e.mode());

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
