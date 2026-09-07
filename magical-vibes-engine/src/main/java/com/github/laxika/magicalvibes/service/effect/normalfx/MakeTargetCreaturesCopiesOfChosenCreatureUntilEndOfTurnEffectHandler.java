package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
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
public class MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentCopierService permanentCopierService;
    private final PlayerInputService playerInputService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect copyEffect =
                (MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect) effect;
        UUID chosenPermanentId = entry.getChosenPermanentId();
        if (chosenPermanentId != null) {
            applyChosenCreature(gameData, entry, copyEffect, chosenPermanentId);
            return;
        }

        List<UUID> creatureIds = creatureIds(gameData, copyEffect.chosenCreaturePredicate());
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() == 1) {
            applyChosenCreature(gameData, entry, copyEffect, creatureIds.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PolymorphousRushCreatureChoice(entry.getControllerId(), copyEffect));
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), creatureIds,
                entry.getCard().getName() + " - Choose a creature to copy.");
    }

    public void completeChoice(GameData gameData, UUID chosenPermanentId,
                               PermanentChoiceContext.PolymorphousRushCreatureChoice context) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || !predicateEvaluationService.matchesPermanentPredicate(
                gameData, chosen, context.effect().chosenCreaturePredicate())) {
            throw new IllegalStateException("Chosen permanent does not match the required creature filter");
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("Polymorphous Rush resolution is no longer pending");
        }
        entry.setChosenPermanentId(chosenPermanentId);
        applyChosenCreature(gameData, entry, context.effect(), chosenPermanentId);
    }

    private void applyChosenCreature(GameData gameData, StackEntry entry,
                                     MakeTargetCreaturesCopiesOfChosenCreatureUntilEndOfTurnEffect effect,
                                     UUID chosenPermanentId) {
        Permanent chosen = gameQueryService.findPermanentById(gameData, chosenPermanentId);
        if (chosen == null || !predicateEvaluationService.matchesPermanentPredicate(
                gameData, chosen, effect.chosenCreaturePredicate())) {
            return;
        }

        List<Permanent> targets = new ArrayList<>();
        for (UUID targetId : entry.targetsForEffect(effect)) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                targets.add(target);
            }
        }

        for (Permanent target : targets) {
            if (!target.isCopyUntilEndOfTurn()) {
                target.setPreCopyCard(target.getCard());
            }
            permanentCopierService.applyCloneCopy(target, chosen, null, null);
            target.setCopyUntilEndOfTurn(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), target.getId(),
                    entry.getControllerId(), new BecomeCopyOfTargetCreatureUntilEndOfTurnEffect(),
                    target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }

        gameLogService.append(gameData, GameLog.text(
                entry.getCard().getName() + " makes " + targets.size() + " target creature(s) copies of "
                        + chosen.getCard().getName() + " until end of turn."));
        log.info("Game {} - {} copies {} onto {} target creature(s)", gameData.id,
                entry.getCard().getName(), chosen.getCard().getName(), targets.size());
    }

    private List<UUID> creatureIds(GameData gameData, PermanentPredicate predicate) {
        List<UUID> ids = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate)) {
                ids.add(permanent.getId());
            }
        });
        return ids;
    }
}
