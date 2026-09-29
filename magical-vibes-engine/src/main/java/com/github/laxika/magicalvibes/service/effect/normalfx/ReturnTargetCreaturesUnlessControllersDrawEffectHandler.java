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
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Decoy Gambit's per-target bounce-or-draw choices. */
@Component
@RequiredArgsConstructor
public class ReturnTargetCreaturesUnlessControllersDrawEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ReturnToHandEffectHandler returnToHandEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnTargetCreaturesUnlessControllersDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnTargetCreaturesUnlessControllersDrawEffect) effect;
        List<UUID> targetIds = e.remainingTargetIds() == null
                ? new ArrayList<>(entry.targetsForEffect(e))
                : new ArrayList<>(e.remainingTargetIds());
        UUID controllerId = e.abilityControllerId() != null
                ? e.abilityControllerId() : entry.getControllerId();
        promptNext(gameData, entry.getCard(), controllerId, targetIds);
    }

    /** Continues the spell after one target's controller has made the choice. */
    public void continueWithRemainingTargets(GameData gameData, Card sourceCard,
                                              ReturnTargetCreaturesUnlessControllersDrawEffect effect) {
        promptNext(gameData, sourceCard, effect.abilityControllerId(),
                new ArrayList<>(effect.remainingTargetIds()));
    }

    /** Returns the target creature when its controller declines to make the caster draw. */
    public void returnTargetCreature(GameData gameData, PendingMayAbility ability) {
        ReturnToHandEffect bounce = ReturnToHandEffect.target();
        StackEntry bounceEntry = new StackEntry(
                StackEntryType.INSTANT_SPELL,
                ability.sourceCard(),
                sourceControllerId(ability),
                ability.sourceCard().getName() + " - return target creature to its owner's hand",
                new ArrayList<>(List.of(bounce)),
                ability.targetCardId(),
                (UUID) null);
        returnToHandEffectHandler.resolve(gameData, bounceEntry, bounce);
    }

    private void promptNext(GameData gameData, Card sourceCard, UUID sourceControllerId,
                            List<UUID> targetIds) {
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
            var nextEffect = new ReturnTargetCreaturesUnlessControllersDrawEffect(
                    targetIds, sourceControllerId);
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
    }

    private UUID sourceControllerId(PendingMayAbility ability) {
        return ability.sourceControllerId() != null
                ? ability.sourceControllerId() : ability.controllerId();
    }
}
