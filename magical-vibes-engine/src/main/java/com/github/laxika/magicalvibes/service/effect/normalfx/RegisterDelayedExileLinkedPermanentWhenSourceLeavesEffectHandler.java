package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Registers the delayed exile trigger of Coffin Queen. The creature the ability just put onto the
 * battlefield is read from, and then cleared off, the source's linked-permanent slot, and the trigger
 * is stored as a {@link DelayedDestroyTargetWhenSourceLeaves} in exile / control-loss mode so it
 * exists independently of the source's abilities (CR 603.7a).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedExileLinkedPermanentWhenSourceLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        UUID linkedId = source.getChosenPermanentId();
        if (linkedId == null) {
            return;
        }
        source.setChosenPermanentId(null);
        gameData.queueDelayedAction(new DelayedDestroyTargetWhenSourceLeaves(
                source.getId(), linkedId, entry.getControllerId(), entry.getCard(), true, false, true, true));
        log.info("Game {} - {} registers delayed trigger: exile {} when it leaves, untaps or is lost",
                gameData.id, entry.getCard().getName(), linkedId);
    }
}
