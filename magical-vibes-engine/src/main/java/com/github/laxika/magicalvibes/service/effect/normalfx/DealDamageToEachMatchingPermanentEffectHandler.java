package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachMatchingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToEachMatchingPermanentEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachMatchingPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry abilityEntry, CardEffect effect) {
        var e = (DealDamageToEachMatchingPermanentEffect) effect;
        // The damage this step deals is added to its result as each damage event is dealt (DamageSupport).
        abilityEntry.recordStep(StepKind.DEAL_DAMAGE, StepOutcome.NONE);
        // "It deals 4 damage to each creature blocking it": the triggering permanent is the source the damage and the
        // predicate are read with.
        StackEntry entry = e.damageSource() == DamageSource.TRIGGERING_PERMANENT
                ? damageSupport.entryDealtByTriggeringPermanent(gameData, abilityEntry) : abilityEntry;
        if (entry == null) {
            return;
        }

        UUID targetPlayerId = entry.getTargetId();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int evaluated = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = evaluated;

        List<Permanent> candidates = new ArrayList<>();
        if (e.scope() == EachPermanentScope.TARGET_PLAYER) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield != null) candidates.addAll(battlefield);
        } else {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                candidates.addAll(battlefield);
            }
        }

        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(source == null ? entry.getCard().getId() : source.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(source)
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withDefendingPlayerId(entry.getDefendingPlayerId())
                .withTriggeringPermanentId(entry.getTriggeringPermanentId());
        Set<UUID> declaredAttackerIds = e.onlyDeclaredAttackers()
                ? entry.getAttackingPermanentSnapshots().stream().map(Permanent::getId).collect(Collectors.toSet())
                : Set.of();
        for (Permanent creature : new ArrayList<>(candidates)) {
            if (e.onlyDeclaredAttackers()
                    ? !declaredAttackerIds.contains(creature.getId())
                    : !predicateEvaluationService.matchesPermanentPredicate(creature, e.predicate(), ctx)) continue;
            // Protection and every prevention are options of the permanent's damage pool (CR 616.1, 702.16e).
            damageSupport.dealCreatureDamage(gameData, entry, creature, rawDamage, source);
        }
        if (entry != abilityEntry) {
            // Dealt through the permanent the trigger event was about: the damage is this step's.
            abilityEntry.addToStep(StepKind.DEAL_DAMAGE, entry.getResolutionResults().outcome(StepKind.DEAL_DAMAGE));
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
