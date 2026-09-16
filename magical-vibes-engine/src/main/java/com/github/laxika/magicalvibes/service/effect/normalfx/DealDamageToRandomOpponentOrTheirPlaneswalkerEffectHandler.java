package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToRandomOpponentOrTheirPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerIdPredicate;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Vial Smasher's random-opponent damage ability. */
@Component
@RequiredArgsConstructor
public class DealDamageToRandomOpponentOrTheirPlaneswalkerEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;
    private final AmountEvaluationService amountEvaluationService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToRandomOpponentOrTheirPlaneswalkerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToRandomOpponentOrTheirPlaneswalkerEffect) effect;
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) return;

        UUID chosenOpponent = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        boolean hasPlaneswalker = gameData.playerBattlefields
                .getOrDefault(chosenOpponent, List.of()).stream()
                .anyMatch(permanent -> gameQueryService.isPlaneswalker(gameData, permanent));
        if (hasPlaneswalker) {
            var targetEffect = new DealDamageToTargetPlayerOrPlaneswalkerEffect(e.damage());
            var targetFilter = new AnyTargetPredicateTargetFilter(
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsPlaneswalkerPredicate(),
                            new PermanentControlledByPlayerPredicate(chosenOpponent))),
                    new PlayerIdPredicate(chosenOpponent),
                    "Target must be that opponent or a planeswalker they control.");
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    entry.getCard(), entry.getControllerId(), List.of(targetEffect), false, targetFilter, 0,
                    entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot(), false, null, chosenOpponent,
                    entry.getControllerId(), entry.getEventValue(), null, true));
            triggerCollectionService.processNextSpellTargetTrigger(gameData);
            return;
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) source = entry.getSourcePermanentSnapshot();
        int damage = amountEvaluationService.evaluate(gameData, e.damage(),
                AmountContext.forStackEntry(entry, source));
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, damage, entry);
        damageSupport.resolveAnyTargetDamage(gameData, entry, chosenOpponent, rawDamage, false);
        gameOutcomeService.checkWinCondition(gameData);
    }
}
