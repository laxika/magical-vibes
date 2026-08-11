package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.GraveyardTargetingSupport;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

/** Resolves a counter removal followed by a reflexive graveyard-targeted effect. */
@Component
@RequiredArgsConstructor
public class RemoveCounterFromSourceThenEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final GameLogService gameLogService;
    private final GraveyardTargetingSupport graveyardTargetingSupport;
    private final PredicateEvaluationService predicateEvaluationService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

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
        gameLogService.append(gameData, GameLog.builder().card(source.getCard())
                .text(" removes a ").text(permanentCounterSupport.counterTypeName(counterType))
                .text(" counter.").build());

        beginReflexiveTrigger(gameData, entry, e.thenEffect());
    }

    private void beginReflexiveTrigger(GameData gameData, StackEntry entry, CardEffect thenEffect) {
        GraveyardTargetingSupport.Target target = graveyardTargetingSupport.findTarget(List.of(thenEffect));
        if (target == null) {
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
            return;
        }

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
