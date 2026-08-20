package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrPermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a bounce target that may be either a spell on the stack or a battlefield permanent.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReturnTargetSpellOrPermanentToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final BounceSupport bounceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetSpellOrPermanentToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        boolean returnedPermanent = false;
        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                bounceSupport.returnSpellToOwnerHand(gameData, entry, targetId);
                continue;
            }

            Card targetCard = target.getCard();
            if (permanentRemovalService.removePermanentToHand(gameData, target)) {
                gameLogService.append(gameData, GameLog.cardThen(targetCard,
                        " is returned to its owner's hand."));
                log.info("Game {} - {} returned to owner's hand by {}", gameData.id,
                        targetCard.getName(), entry.getCard().getName());
                returnedPermanent = true;
            }
        }

        if (returnedPermanent) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
