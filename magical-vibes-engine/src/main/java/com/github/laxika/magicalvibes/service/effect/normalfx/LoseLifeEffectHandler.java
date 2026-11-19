package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the whole life-loss family via {@link LoseLifeEffect}: {@link SubjectResolutionService} finds who loses
 * life and {@code controllerGainsLifeLost} drains the total life lost back to the controller. The amount is evaluated
 * once against the stack entry (source-relative amounts use the live source permanent when present, else the
 * last-known snapshot), with the receiving player as the amount's target for "defending player" and as its
 * controller for "its owner" of the source; several target players each get the amount evaluated against them.
 *
 * <p>Every player goes through {@link LifeSupport#applyLifeLosses}, which records the amount lost and fires "loses
 * life" triggers. Life loss is never routed through damage plumbing.
 *
 * <p>Marked {@link AsksDeathReplacementsFirst}: the life losses are its first change, so a Lich's Duel Mastery one of
 * them would sacrifice instead is asked about (CR 616.1) before any life is lost.
 */
@Component
@RequiredArgsConstructor
public class LoseLifeEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LoseLifeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LoseLifeEffect) effect;
        PlayerSubject subject = e.player();

        // Source-relative amounts (e.g. "for each Vampire you control") use the live source
        // permanent when present, else the last-known snapshot.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        List<UUID> players = subjectResolutionService.players(gameData, entry, e, subject);
        if (e.exemptIfControls() != null
                && (subject instanceof PlayerSubject.Each || subject instanceof PlayerSubject.OpponentsOf)) {
            // "each opponent who doesn't control an Elf" — players controlling a matching
            // permanent are skipped entirely (Thornbow Archer).
            players = players.stream()
                    .filter(playerId -> !controlsMatching(gameData, playerId, e.exemptIfControls()))
                    .toList();
        }

        AmountContext amountContext = AmountContext.forStackEntry(entry, source);
        if (subject instanceof PlayerSubject.DefendingPlayer && !players.isEmpty()) {
            amountContext = amountContext.withTargetPermanentId(players.getFirst());
        }
        if (subject instanceof PlayerSubject.OwnerOf(PermanentSubject.Source ignored) && !players.isEmpty()) {
            amountContext = amountContext.withControllerId(players.getFirst());
        }
        int amount = amountEvaluationService.evaluate(gameData, e.amount(), amountContext);
        boolean amountPerTarget = subject instanceof PlayerSubject.TargetPlayer target
                && (target.group() == null ? entry.targetsForEffect(e) : entry.targetsForGroup(target.group()))
                .size() > 1;

        Map<UUID, Integer> losses = new LinkedHashMap<>();
        for (UUID playerId : players) {
            losses.put(playerId, amountPerTarget
                    ? amountEvaluationService.evaluate(gameData, e.amount(),
                    amountContext.withTargetPermanentId(playerId))
                    : amount);
        }
        int totalLifeLost = lifeSupport.applyLifeLosses(gameData, losses, entry.getCard().getName());

        if (e.recordTargetLifeLost()) {
            entry.setEventValue(totalLifeLost);
        }
        if (totalLifeLost > 0 && controllerGainsLifeLost(gameData, entry, e)) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(), totalLifeLost);
        }
    }

    private boolean controlsMatching(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        var battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        return battlefield.stream()
                .anyMatch(permanent -> predicateEvaluationService.matchesPermanentPredicate(gameData, permanent, predicate));
    }

    private boolean controllerGainsLifeLost(GameData gameData, StackEntry entry, LoseLifeEffect effect) {
        return effect.controllerGainsLifeLost()
                && (effect.controllerGainsLifeLostCondition() == null
                || conditionEvaluationService.isMet(gameData, effect.controllerGainsLifeLostCondition(),
                ConditionContext.forStackEntry(entry)));
    }
}
