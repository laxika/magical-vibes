package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPermanent == null) {
            log.info("Game {} - temporary permanent copy target no longer exists", gameData.id);
            return;
        }

        var copyEffect = (EachOtherPermanentMatchingPredicateBecomesCopyOfTargetPermanentUntilEndOfTurnEffect) effect;
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());
        if (!predicateEvaluationService.matchesPermanentPredicate(
                targetPermanent, copyEffect.targetPredicate(), filterContext)) {
            log.info("Game {} - temporary permanent copy target is no longer legal", gameData.id);
            return;
        }

        List<Permanent> permanentsToCopy = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getId().equals(targetId)
                    && predicateEvaluationService.matchesPermanentPredicate(
                            permanent, copyEffect.affectedPredicate(), filterContext)) {
                permanentsToCopy.add(permanent);
            }
        });

        String targetName = targetPermanent.getCard().getName();
        for (Permanent permanent : permanentsToCopy) {
            if (!permanent.isCopyUntilEndOfTurn()) {
                permanent.setPreCopyCard(permanent.getCard());
            }
            permanentCopierService.applyCloneCopy(permanent, targetPermanent, null, null);
            if (copyEffect.removeLegendary()) {
                var supertypes = EnumSet.noneOf(CardSupertype.class);
                supertypes.addAll(permanent.getCard().getSupertypes());
                supertypes.remove(CardSupertype.LEGENDARY);
                permanent.getCard().setSupertypes(supertypes);
            }
            permanent.setCopyUntilEndOfTurn(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), permanent.getId(),
                    entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes " + permanentsToCopy.size() + " other permanent(s) a copy of "
                        + targetName + " until end of turn.")
                .build());
        log.info("Game {} - {} copies onto {} permanents", gameData.id, targetName, permanentsToCopy.size());
    }
}
