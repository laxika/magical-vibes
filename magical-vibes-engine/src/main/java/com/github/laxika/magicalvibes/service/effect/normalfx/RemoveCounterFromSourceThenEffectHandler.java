package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetPredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves a counter removal followed by a reflexive effect. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final TargetPredicateEvaluationService targetPredicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveCounterFromSourceThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RemoveCounterFromSourceThenEffect) effect;
        UUID sourcePermanentId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId()
                : entry.getTargetId();
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null) {
            return;
        }

        CounterType counterType = findCounterType(source, e.counterType());
        if (counterType == null) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - 1);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, 1);
        }
        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" removes a ").text(permanentCounterSupport.counterTypeName(counterType))
                .text(" counter.").build());

        beginReflexiveTrigger(gameData, entry, e.thenEffect());
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(List.of(thenEffect));
        if (target != null) {
            beginGraveyardReflexiveTrigger(gameData, entry, thenEffect, target);
            return;
        }

        TargetSpec targetSpec = thenEffect.targetSpec();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)
                || targetSpec.admits(TargetPredicate.Kind.PLAYER)) {
            beginBattlefieldReflexiveTrigger(gameData, entry, thenEffect, targetSpec);
            return;
        }

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(thenEffect),
                0,
                null,
                entry.getSourcePermanentId(),
                null,
                null,
                null,
                null));
    }

    private void beginBattlefieldReflexiveTrigger(GameData gameData, StackEntry entry,
                                                   CardEffect thenEffect, TargetSpec targetSpec) {
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<UUID> validPermanentTargets = new ArrayList<>();
        if (targetSpec.admits(TargetPredicate.Kind.PERMANENT)) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent permanent : battlefield) {
                    if (targetPredicateEvaluationService.matchesPermanent(
                            targetPredicate, permanent, filterContext)) {
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
                entry.getCard(), entry.getControllerId(), List.of(thenEffect),
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot()));
        String prompt = entry.getCard().getName() + "'s reflexive ability - Choose target.";
        if (validPlayerTargets.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), validPermanentTargets, prompt);
        } else {
            playerInputService.beginAnyTargetChoice(gameData, entry.getControllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);
        }
    }

    private void beginGraveyardReflexiveTrigger(GameData gameData, StackEntry entry,
                                                 CardEffect thenEffect,
                                                 GraveyardTargetingSupport.Target target) {

        UUID controllerId = entry.getControllerId();
        List<Card> matchingCards = new ArrayList<>();
        for (UUID graveyardOwnerId : target.scope().graveyardOwners(gameData.orderedPlayerIds, controllerId)) {
            List<Card> graveyard = gameData.playerGraveyards.get(graveyardOwnerId);
            if (graveyard == null) {
                continue;
            }
            for (Card card : graveyard) {
                if (predicateEvaluationService.matchesCardPredicate(
                        card, target.filter(), entry.getCard().getId())) {
                    matchingCards.add(card);
                }
            }
        }

        if (matchingCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability has no legal graveyard target."));
            return;
        }

        if (matchingCards.size() == 1) {
            putTargetedReflexiveTriggerOnStack(gameData, entry, thenEffect, matchingCards.getFirst().getId());
            return;
        }

        List<Integer> indices = IntStream.range(0, matchingCards.size()).boxed().toList();
        interactionHandlerRegistry.begin(gameData, PendingInteraction.GraveyardChoice
                .builder(controllerId, indices, GraveyardChoiceDestination.MAY_ABILITY_TARGET,
                        "Choose a card from your graveyard to return to the battlefield.")
                .cardPool(matchingCards)
                .mayAbilityContext(entry.getCard(), controllerId, List.of(thenEffect), entry.getSourcePermanentId())
                .build());
    }

    private void putTargetedReflexiveTriggerOnStack(GameData gameData, StackEntry entry,
                                                     CardEffect thenEffect, UUID targetCardId) {
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(thenEffect),
                0,
                targetCardId,
                entry.getSourcePermanentId(),
                null,
                Zone.GRAVEYARD,
                null,
                null));
    }

    private CounterType findCounterType(Permanent source, CounterType requestedType) {
        if (requestedType != CounterType.ANY) {
            return source.getCounterCount(requestedType) > 0 ? requestedType : null;
        }
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            if (source.getCounterCount(counterType) > 0) {
                return counterType;
            }
        }
        return null;
    }
}
