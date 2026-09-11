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
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
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

        if (e.counterType() == CounterType.ANY) {
            var options = java.util.Arrays.stream(CounterType.values())
                    .filter(type -> type != CounterType.ANY && type != CounterType.SILVER
                            && source.getCounterCount(type) >= e.count())
                    .map(type -> new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect.ChooseOneOption(
                            "Remove " + e.count() + " " + permanentCounterSupport.counterTypeName(type) + " counters",
                            new RemoveCounterFromSourceThenEffect(type, e.count(), e.thenEffect(),
                                    e.onlyIfLastCounterRemoved())))
                    .toList();
            if (options.size() > 1) {
                playerInputService.beginChooseModeChoice(gameData, entry.getControllerId(), entry.getCard(),
                        new com.github.laxika.magicalvibes.model.effect.ChooseOneEffect(options),
                        false, entry.getSourcePermanentId());
                return;
            }
        }
        CounterType counterType = findCounterType(source, e.counterType(), e.count());
        if (counterType == null) {
            return;
        }

        source.setCounterCount(counterType, source.getCounterCount(counterType) - e.count());
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(source, e.count());
        }
        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" removes ").text(e.count() == 1 ? "a " : e.count() + " ")
                .text(permanentCounterSupport.counterTypeName(counterType))
                .text(e.count() == 1 ? " counter." : " counters.").build());

        if (!e.onlyIfLastCounterRemoved() || source.getCounterCount(counterType) == 0) {
            beginReflexiveTrigger(gameData, entry, e.thenEffect());
        }
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        if (thenEffect instanceof CombatDamageTriggerContextEffect contextEffect
                && contextEffect.combatDamageTriggerContext()
                == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER
                && entry.getTargetId() != null) {
            putPlayerTargetedReflexiveTriggerOnStack(gameData, entry, thenEffect, entry.getTargetId());
            return;
        }

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

        StackEntry reflexive = new StackEntry(
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
                null);
        reflexive.setTriggeringCardId(entry.getTriggeringCardId());
        gameData.stack.add(reflexive);
    }

    private void beginBattlefieldReflexiveTrigger(GameData gameData, StackEntry entry,
                                                   CardEffect thenEffect, TargetSpec targetSpec) {
        TargetPredicate targetPredicate = targetSpec.targetPredicate();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId())
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId())
                .withDefendingPlayerId(entry.getTargetId());
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
                entry.getSourcePermanentId(), entry.getSourcePermanentSnapshot(), 0, 0, false, entry.getTargetId()));
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
            putGraveyardTargetedReflexiveTriggerOnStack(
                    gameData, entry, thenEffect, matchingCards.getFirst().getId());
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

    private void putGraveyardTargetedReflexiveTriggerOnStack(GameData gameData, StackEntry entry,
                                                     CardEffect thenEffect, UUID targetCardId) {
        StackEntry reflexive = new StackEntry(
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
                null);
        reflexive.setTriggeringCardId(entry.getTriggeringCardId());
        gameData.stack.add(reflexive);
    }

    private void putPlayerTargetedReflexiveTriggerOnStack(GameData gameData, StackEntry entry,
                                                           CardEffect thenEffect, UUID targetPlayerId) {
        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                entry.getCard(),
                entry.getControllerId(),
                entry.getCard().getName() + "'s ability",
                List.of(thenEffect),
                targetPlayerId,
                entry.getSourcePermanentId()));
    }

    private CounterType findCounterType(Permanent source, CounterType requestedType, int count) {
        if (requestedType != CounterType.ANY) {
            return source.getCounterCount(requestedType) >= count ? requestedType : null;
        }
        for (CounterType counterType : CounterType.values()) {
            if (counterType == CounterType.ANY || counterType == CounterType.SILVER) {
                continue;
            }
            if (source.getCounterCount(counterType) >= count) {
                return counterType;
            }
        }
        return null;
    }
}
