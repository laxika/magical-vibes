package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenMayCopyEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component("returnTargetPermanentToHandThenMayCopyNormalEffectHandler")
@RequiredArgsConstructor
public class ReturnTargetPermanentToHandThenMayCopyEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final MaySacrificeForCounterSupport maySacrificeForCounterSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentToHandThenMayCopyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        if (permanentRemovalService.removePermanentToHand(gameData, target)) {
            gameLogService.append(gameData,
                    GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
            log.info("Game {} - {} returned to owner's hand by {}",
                    gameData.id, target.getCard().getName(), entry.getCard().getName());
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (!maySacrificeForCounterSupport.matchingPermanentIds(
                gameData, targetControllerId, new PermanentIsLandPredicate()).isEmpty()) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    targetControllerId,
                    List.of(effect),
                    entry.getCard().getName() + " - Sacrifice a land?"));
        }
    }
}
