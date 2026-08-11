package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentAndAllWithSameNameToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReturnTargetPermanentAndAllWithSameNameToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetPermanentAndAllWithSameNameToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        var sameNameEffect = (ReturnTargetPermanentAndAllWithSameNameToHandEffect) effect;
        String targetName = target.getCard().getName();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getName().equals(targetName)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                permanent, sameNameEffect.sameNamePredicate(), filterContext)) {
                    toReturn.add(permanent);
                }
            }
        });

        for (Permanent permanent : toReturn) {
            permanentRemovalService.removePermanentToHand(gameData, permanent);
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " is returned to its owner's hand."));
        }

        if (!toReturn.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
