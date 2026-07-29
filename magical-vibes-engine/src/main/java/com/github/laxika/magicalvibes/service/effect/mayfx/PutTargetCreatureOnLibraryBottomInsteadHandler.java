package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Ether Well's "you may put it on the bottom of its owner's library instead" decision: accepting
 * sends the creature to the bottom, declining falls back to the top placement the spell would have
 * done anyway.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetCreatureOnLibraryBottomInsteadHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final InputCompletionService inputCompletionService;
    private final PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffectHandler topPlacement;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCreatureOnTopOrOptionalBottomOfLibraryEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Permanent target = gameQueryService.findPermanentById(gameData, ability.targetCardId());
        if (target != null) {
            if (accepted) {
                if (permanentRemovalService.removePermanentToLibraryBottom(gameData, target)) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(target.getCard(), " is put on the bottom of its owner's library."));
                    log.info("Game {} - {} put on bottom of library by {}", gameData.id,
                            target.getCard().getName(), ability.sourceCard().getName());
                }
                permanentRemovalService.removeOrphanedAuras(gameData);
            } else {
                topPlacement.putOnTop(gameData, target, ability.sourceCard().getName());
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
