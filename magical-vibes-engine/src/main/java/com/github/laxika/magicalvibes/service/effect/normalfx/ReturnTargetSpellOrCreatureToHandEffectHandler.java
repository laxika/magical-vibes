package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrCreatureToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link ReturnTargetSpellOrCreatureToHandEffect} for either a battlefield creature or a
 * spell on the stack.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnTargetSpellOrCreatureToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetSpellOrCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            Card targetCard = target.getCard();
            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                gameLogService.append(gameData, GameLog.cardThen(targetCard,
                        " is returned to its owner's hand."));
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                        targetCard.getName(), entry.getCard().getName());
            }
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        bounceSupport.returnSpellToOwnerHand(gameData, entry, targetId);
    }
}
