package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerThenIfMilledEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
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
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;
    private final PlayerInputService playerInputService;

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

        CardEffect followUp = matched ? e.thenEffect() : e.elseEffect();
        if (followUp != null) {
            if (matched && e.thenEffectTargets()) {
                beginReflexiveTargetChoice(gameData, entry, followUp);
                return;
            }
            dispatch(gameData, entry, followUp);
        }
    }

    private void beginReflexiveTargetChoice(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetSpec targetSpec = effect.targetSpec();
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> validPermanents = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            gameData.playerBattlefields.values().stream()
                    .flatMap(List::stream)
                    .filter(permanent -> targetPredicateEvaluationService.matchesPermanent(
                            targetPredicate, permanent, filterContext))
                    .map(Permanent::getId)
                    .forEach(validPermanents::add);
        }
        List<UUID> validPlayers = targetSpec.admits(TargetPredicate.Kind.PLAYER)
                ? gameData.orderedPlayerIds.stream()
                        .filter(playerId -> targetPredicateEvaluationService.matchesPlayer(
                                targetPredicate, playerId, entry.getControllerId(), gameData))
                        .toList()
                : List.of();
        if (validPermanents.isEmpty() && validPlayers.isEmpty()) {
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.MayAbilityTriggerTarget(
                entry.getCard(), entry.getControllerId(), List.of(effect), entry.getSourcePermanentId(),
                entry.getSourcePermanentSnapshot(), entry.getEventValue(), entry.getXValue()));
        String prompt = entry.getCard().getName() + "'s reflexive ability - Choose target.";
        if (validPlayers.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanents, prompt);
        } else {
            playerInputService.beginAnyTargetChoice(
                    gameData, entry.getControllerId(), validPermanents, validPlayers, prompt);
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
