package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatRequirement;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetIfDidNotAttackAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.MustAttackUnlessControllerPaysManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.SetCombatRequirementThisTurnEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MustAttackUnlessControllerPaysManaValueEffect}: prompts the active player to pay
 * {X} (X = the target creature's mana value) via the may-ability system. Declining — or accepting
 * without enough mana — applies the penalty in {@link #applyPenalty}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MustAttackUnlessControllerPaysManaValueEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SetCombatRequirementThisTurnEffectHandler setCombatRequirementThisTurnEffectHandler;
    private final DestroyTargetIfDidNotAttackAtEndStepEffectHandler destroyTargetIfDidNotAttackAtEndStepEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MustAttackUnlessControllerPaysManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPermanentId = entry.getTargetId();
        Permanent target = gameQueryService.findPermanentById(gameData, targetPermanentId);
        if (target == null) {
            // Target left the battlefield before resolution — the ability does nothing.
            return;
        }

        int manaValue = target.getCard().getManaValue();
        String cost = "{" + manaValue + "}";
        String prompt = "Pay " + cost + "? If you don't, " + target.getCard().getName()
                + " attacks this turn if able and is destroyed at the beginning of the next end step"
                + " if it didn't attack. (" + entry.getCard().getName() + ")";

        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(), gameData.activePlayerId, List.of((CardEffect) effect), prompt,
                targetPermanentId, cost, entry.getSourcePermanentId()));
    }

    /**
     * "The creature attacks this turn if able, and at the beginning of the next end step, destroy
     * it if it didn't attack this turn."
     */
    public void applyPenalty(GameData gameData, Card sourceCard, UUID abilityControllerId, UUID targetPermanentId) {
        SetCombatRequirementThisTurnEffect mustAttack =
                new SetCombatRequirementThisTurnEffect(CombatRequirement.MUST_ATTACK);
        DestroyTargetIfDidNotAttackAtEndStepEffect destroy = new DestroyTargetIfDidNotAttackAtEndStepEffect();
        StackEntry syntheticEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY, sourceCard, abilityControllerId,
                sourceCard.getName() + " - must attack or be destroyed",
                List.of(mustAttack, destroy), targetPermanentId, (UUID) null);
        setCombatRequirementThisTurnEffectHandler.resolve(gameData, syntheticEntry, mustAttack);
        destroyTargetIfDidNotAttackAtEndStepEffectHandler.resolve(gameData, syntheticEntry, destroy);
    }
}
