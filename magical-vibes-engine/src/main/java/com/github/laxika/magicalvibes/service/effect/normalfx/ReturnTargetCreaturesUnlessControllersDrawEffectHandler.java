package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCreaturesUnlessControllersDrawEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Decoy Gambit's per-target bounce-or-draw choices. Each creature's controller is asked in turn order
 * (CR 101.4). Once every choice is made, the caster draws one card per "draw" answer and then the creatures
 * chosen to return leave the battlefield together (Decoy Gambit ruling, 2020-04-17).
 */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreaturesUnlessControllersDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ReturnToHandEffectHandler returnToHandEffectHandler;
    private final DrawService drawService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreaturesUnlessControllersDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreaturesUnlessControllersDrawEffect) effect;
        UUID controllerId = e.abilityControllerId() != null
                ? e.abilityControllerId() : entry.getControllerId();
        List<UUID> targetIds = e.remainingTargetIds() == null
                ? turnOrderTargets(gameData, controllerId, entry.targetsForEffect(e))
                : new ArrayList<>(e.remainingTargetIds());
        promptNext(gameData, entry.getCard(), controllerId, targetIds, new ArrayList<>(), 0);
    }

    /** Records one target's controller answer and continues the spell with the next target. */
    public void continueAfterChoice(GameData gameData, PendingMayAbility ability, boolean accepted) {
        var effect = (ReturnTargetCreaturesUnlessControllersDrawEffect) ability.effects().getFirst();
        List<UUID> returnTargetIds = new ArrayList<>(effect.returnTargetIds());
        int drawCount = effect.drawCount();
        if (accepted) {
            drawCount++;
        } else {
            returnTargetIds.add(ability.targetCardId());
        }
        promptNext(gameData, ability.sourceCard(), effect.abilityControllerId(),
                new ArrayList<>(effect.remainingTargetIds()), returnTargetIds, drawCount);
    }

    private void promptNext(GameData gameData, Card sourceCard, UUID sourceControllerId,
                            List<UUID> targetIds, List<UUID> returnTargetIds, int drawCount) {
        if (sourceControllerId == null) {
            return;
        }
        while (!targetIds.isEmpty()) {
            UUID targetId = targetIds.removeFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || !gameQueryService.isCreature(gameData, target)) {
                continue;
            }
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (targetControllerId == null) {
                continue;
            }
            if (drawService.isDrawPrevented(gameData, sourceControllerId)) {
                returnTargetIds.add(targetId);
                continue;
            }
            var nextEffect = new ReturnTargetCreaturesUnlessControllersDrawEffect(
                    targetIds, sourceControllerId, returnTargetIds, drawCount);
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    sourceCard,
                    targetControllerId,
                    List.of(nextEffect),
                    "Have " + sourceCard.getName() + " have you draw a card instead of returning "
                            + target.getCard().getName() + " to its owner's hand?",
                    targetId,
                    sourceControllerId));
            return;
        }
        finish(gameData, sourceCard, sourceControllerId, returnTargetIds, drawCount);
    }

    private void finish(GameData gameData, Card sourceCard, UUID sourceControllerId,
                        List<UUID> returnTargetIds, int drawCount) {
        if (drawCount > 0) {
            playerInteractionSupport.applyDrawCards(gameData, sourceControllerId, drawCount);
        }
        for (UUID returnTargetId : returnTargetIds) {
            if (gameQueryService.findPermanentById(gameData, returnTargetId) != null) {
                returnTargetCreature(gameData, sourceCard, sourceControllerId, returnTargetId);
            }
        }
    }

    private void returnTargetCreature(GameData gameData, Card sourceCard, UUID sourceControllerId, UUID targetId) {
        ReturnToHandEffect bounce = ReturnToHandEffect.target();
        StackEntry bounceEntry = new StackEntry(
                StackEntryType.INSTANT_SPELL,
                sourceCard,
                sourceControllerId,
                sourceCard.getName() + " - return target creature to its owner's hand",
                new ArrayList<>(List.of(bounce)),
                targetId,
                (UUID) null);
        returnToHandEffectHandler.resolve(gameData, bounceEntry, bounce);
    }

    /** Orders the chosen creatures by their controllers' turn order starting with the active player (APNAP). */
    private List<UUID> turnOrderTargets(GameData gameData, UUID casterId, List<UUID> targetIds) {
        List<UUID> ordered = new ArrayList<>();
        for (UUID opponentId : AnyOpponentMayTakeDamageSacrificeSourceEffectHandler.apnapOpponents(gameData, casterId)) {
            for (UUID targetId : targetIds) {
                if (opponentId.equals(gameQueryService.findPermanentController(gameData, targetId))
                        && !ordered.contains(targetId)) {
                    ordered.add(targetId);
                }
            }
        }
        return ordered;
    }
}
