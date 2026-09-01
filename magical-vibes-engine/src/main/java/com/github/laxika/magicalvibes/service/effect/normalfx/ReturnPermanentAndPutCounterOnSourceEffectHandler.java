package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentAndPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnPermanentAndPutCounterOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnPermanentAndPutCounterOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnPermanentAndPutCounterOnSourceEffect) effect;
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentId(entry.getSourcePermanentId());

        List<UUID> validIds = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) {
                    validIds.add(permanent.getId());
                }
            }
        }

        if (!validIds.isEmpty()) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ReturnPermanentAndPutCounterOnSource(
                            controllerId, entry.getCard(), entry.getSourcePermanentId()));
            playerInputService.beginPermanentChoice(gameData, controllerId, validIds,
                    entry.getCard().getName() + " — Choose " + e.permanentDescription()
                            + " to return to its owner's hand.");
            return;
        }

        if (entry.getSourcePermanentId() == null
                || findPermanent(gameData, entry.getSourcePermanentId()) == null) {
            return;
        }

        MayEffect selfReturn = new MayEffect(
                ReturnToHandEffect.self(),
                "Return this creature to its owner's hand?");
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            throw new IllegalStateException("Return-and-counter effect is not part of the resolving stack entry");
        }
        entry.replaceEffectToResolve(effectIndex, selfReturn);
        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                controllerId,
                List.of(selfReturn.wrapped()),
                entry.getCard().getName() + " - " + selfReturn.prompt(),
                null,
                null,
                entry.getSourcePermanentId(),
                null,
                0,
                0,
                entry.getAttackedTargetId(),
                null));
        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " has no other creature to return; asking about returning itself."));
        log.info("Game {} - {} has no other creature to return", gameData.id, entry.getCard().getName());
    }

    private Permanent findPermanent(GameData gameData, UUID permanentId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(permanentId)) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
