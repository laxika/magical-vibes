package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayReturnPermanentToHandAndEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves the selected permanent return and resumes the entering creature's ETB processing. */
@Slf4j
@Component
@RequiredArgsConstructor
public class MayReturnPermanentToHandAndEnterWithCountersEffectHandler implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayReturnPermanentToHandAndEnterWithCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choice = (MayReturnPermanentToHandAndEnterWithCountersEffect) effect;
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent target = entry.getTargetId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getTargetId());

        boolean returned = false;
        if (source != null && target != null && predicateEvaluationService.matchesPermanentPredicate(
                target,
                choice.filter(),
                FilterContext.of(gameData)
                        .withSourceCardId(entry.getCard().getId())
                        .withSourceControllerId(entry.getControllerId())
                        .withSourcePermanentId(source.getId()))) {
            returned = permanentRemovalService.removePermanentToHand(gameData, target);
            if (returned) {
                gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " is returned to its owner's hand."));
                permanentRemovalService.removeOrphanedAuras(gameData);
                permanentCounterSupport.placeCounterOnPermanent(
                        gameData, entry, source, choice.counterType(), choice.counterCount());
                log.info("Game {} - {} returned {} to its owner's hand", gameData.id,
                        entry.getCard().getName(), target.getCard().getName());
            }
        }

        if (!returned) {
            log.info("Game {} - {} did not return a permanent", gameData.id, entry.getCard().getName());
        }

        boolean wasCastFromHand = source != null && source.isCast() && source.getCastFromZone() == Zone.HAND;
        battlefieldEntryService.processCreatureETBEffects(
                gameData, entry.getControllerId(), entry.getCard(), null, wasCastFromHand);
    }
}
