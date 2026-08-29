package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MillControllerThenIfMilledEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillControllerThenIfMilledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (MillControllerThenIfMilledEffect) effect;
        UUID controllerId = entry.getControllerId();

        // resolveMillPlayer returns only the cards that actually reached the graveyard, which is
        // exactly what "milled this way" means — a card diverted by a replacement does not count.
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        int count = Math.max(0, amountEvaluationService.evaluate(gameData, e.count(),
                AmountContext.forStackEntry(entry, source)));
        List<Card> milled = graveyardService.resolveMillPlayer(gameData, controllerId, count);
        int matchCount = (int) milled.stream()
                .filter(card -> predicateEvaluationService.matchesCardPredicate(card, e.filter(), null))
                .count();
        entry.setEventValue(matchCount);
        boolean matched = e.requireAllCardsMilled() ? milled.size() == count : matchCount > 0;

        log.info("Game {} - {} milled {} card(s), condition {}",
                gameData.id, entry.getCard().getName(), milled.size(), matched ? "met" : "not met");

        if (matched) {
            if (e.thenEffectTargets()) {
                queueTargetedReflexiveAbility(gameData, entry, e.thenEffect());
            } else {
                dispatch(gameData, entry, e.thenEffect());
            }
        }
    }

    private void queueTargetedReflexiveAbility(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        TargetSpec targetSpec = thenEffect.targetSpec();
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        List<UUID> validPermanentTargets = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(entry.getCard().getId())
                    .withSourceControllerId(entry.getControllerId());
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (targetPredicateEvaluationService.matchesPermanent(targetPredicate, permanent, filterContext)) {
                        validPermanentTargets.add(permanent.getId());
                    }
                }
            }
        }

        List<UUID> validPlayerTargets = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                ? gameData.orderedPlayerIds.stream()
                        .filter(playerId -> targetPredicateEvaluationService.matchesPlayer(
                                targetPredicate, playerId, entry.getControllerId(), gameData))
                        .toList()
                : List.of();
        if (validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(thenEffect)));
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    entry.getCard().getName() + "'s reflexive ability - Choose target.");
        } else {
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(), validPermanentTargets,
                    validPlayerTargets, entry.getCard().getName() + "'s reflexive ability - Choose target.");
        }
    }

    private void dispatch(GameData gameData, StackEntry entry, CardEffect effect) {
        if (effect instanceof SequenceEffect sequence) {
            for (CardEffect step : sequence.steps()) {
                dispatch(gameData, entry, step);
            }
            return;
        }

        EffectHandler handler = effectHandlerRegistry.getHandler(effect);
        if (handler != null) {
            handler.resolve(gameData, entry, effect);
        } else {
            log.warn("No handler for follow-up effect in MillControllerThenIfMilledEffect: {}",
                    effect.getClass().getSimpleName());
        }
    }
}
