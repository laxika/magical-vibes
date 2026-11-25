package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ScryBeforeExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExploreEffect}. A set subject ("each Merfolk creature you control explores") makes each permanent it
 * names as the effect resolves explore one at a time, its controller choosing which explores next (CR 701.44d).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExploreEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        ExploreEffect exploreEffect = (ExploreEffect) effect;
        if (exploreEffect.subject() != null && exploreEffect.subject().isSet()) {
            exploreEachInTurn(gameData, entry, subjectResolutionService.permanentIds(
                    gameData, entry, exploreEffect, exploreEffect.subject()));
            return;
        }
        UUID exploringPermanentId = findExploringPermanentId(gameData, entry, exploreEffect);
        if (exploreEffect.permanentId() != null
                && gameQueryService.findPermanentById(gameData, exploreEffect.permanentId()) == null) {
            return;
        }
        if (exploreEffect.subject() != null && exploringPermanentId == null) {
            return;
        }
        Permanent exploringPermanent = exploringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, exploringPermanentId);
        UUID controllerId = exploringPermanent == null
                ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, exploringPermanent.getId());
        int exploreCount = exploreEffect.amount() == null
                ? 1
                : Math.max(0, amountEvaluationService.evaluate(gameData, exploreEffect.amount(),
                        AmountContext.forStackEntry(entry, exploringPermanent)));
        if (exploreCount == 0) {
            return;
        }
        if (!exploreEffect.replacementApplied()) {
            ExploreReplacementCounts replacements = countExploreReplacements(gameData, exploringPermanent);
            int effectiveExploreCount = exploreCount * (1 << replacements.doubleExploreCount());
            if (replacements.scryCount() > 0 || replacements.doubleExploreCount() > 0) {
                List<CardEffect> replacementEffects = new ArrayList<>(
                        effectiveExploreCount * (replacements.scryCount() + 1));
                for (int i = 0; i < effectiveExploreCount; i++) {
                    for (int j = 0; j < replacements.scryCount(); j++) {
                        replacementEffects.add(new ScryEffect(1));
                    }
                    replacementEffects.add(ExploreEffect.afterReplacement(
                            exploreEffect.targeted(), exploreEffect.subject(), exploreEffect.permanentId(),
                            exploreEffect.targetPredicate()));
                }
                insertEffectsAfter(entry, effect, replacementEffects);
                return;
            }
        }
        insertRemainingExplores(entry, effect, exploreEffect, exploreCount);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = FaceDownVisibility.sourceName(gameData, entry);

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + " explores)."));
            putExploreCounter(gameData, entry, exploringPermanent, controllerId);
            gameData.playersWhoControlledPermanentThatExploredThisTurn.add(controllerId);
            ActionEvents.explored(gameData, controllerId, null);
            return;
        }

        Card topCard = deck.getFirst();
        // The explore event happened even when its revealed card is later handled by the land or
        // nonland branch. Remember the controller for cards that check this history this turn.
        if (controllerId != null) {
            gameData.playersWhoControlledPermanentThatExploredThisTurn.add(controllerId);
        }

        // Reveal the top card to all players
        gameLogService.append(gameData, GameLog.textCardText(sourceName + " explores — " + playerName + " reveals ", topCard, "."));

        if (topCard.hasType(CardType.LAND)) {
            // Land — put into controller's hand
            deck.removeFirst();
            gameData.addCardToHand(controllerId, topCard);
            gameLogService.append(gameData, GameLog.textCardText(playerName + " puts ", topCard, " into their hand."));
            log.info("Game {} - {} explores, reveals land {} — to hand",
                    gameData.id, sourceName, topCard.getName());
            // Explore is complete — check for "whenever a creature you control explores" triggers
            ActionEvents.explored(gameData, controllerId, topCard);
        } else {
            // Not a land — put a +1/+1 counter on the exploring creature
            putExploreCounter(gameData, entry, exploringPermanent, controllerId);

            // Ask: put the revealed card into your graveyard?
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(), controllerId,
                    List.of(new ExploreEffect()),
                    sourceName + " — Put " + topCard.getName() + " into your graveyard?"
            ));

            log.info("Game {} - {} explores, reveals non-land {} — +1/+1 counter, may graveyard",
                    gameData.id, sourceName, topCard.getName());
        }
    
    }

    private void putExploreCounter(GameData gameData, StackEntry entry, Permanent source, UUID controllerId) {
        if (source != null && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
            int placed = gameQueryService.doublePlusOnePlusOneCounters(gameData, source, 1);
            if (placed > 0) {
                source.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + placed);
                permanentCounterSupport.notifyCountersPlaced(
                        gameData, entry, source, placed, CounterType.PLUS_ONE_PLUS_ONE);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnCreature(
                        gameData, source, controllerId);
                permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                        gameData, source, placed);
                permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                        gameData, source, placed, controllerId);
                gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                        placed == 1 ? " gets a +1/+1 counter." : " gets " + placed + " +1/+1 counters."));
            }
        }

    }

    private ExploreReplacementCounts countExploreReplacements(GameData gameData, Permanent exploringPermanent) {
        if (exploringPermanent == null
                || !gameQueryService.isCreature(gameData, exploringPermanent)) {
            return new ExploreReplacementCounts(0, 0);
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, exploringPermanent.getId());
        if (controllerId == null) {
            return new ExploreReplacementCounts(0, 0);
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return new ExploreReplacementCounts(0, 0);
        }
        int scryCount = 0;
        int doubleExploreCount = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect staticEffect : gameQueryService.getActiveStaticEffects(gameData, permanent, controllerId)) {
                if (staticEffect instanceof ScryBeforeExploreReplacementEffect) {
                    scryCount++;
                } else if (staticEffect instanceof DoubleExploreReplacementEffect) {
                    doubleExploreCount++;
                }
            }
        }
        return new ExploreReplacementCounts(scryCount, doubleExploreCount);
    }

    private record ExploreReplacementCounts(int scryCount, int doubleExploreCount) {
    }

    /** One of several permanents told to explore at once explores, the controller choosing which when there is a choice. */
    private void exploreEachInTurn(GameData gameData, StackEntry entry, List<UUID> exploringIds) {
        if (exploringIds.size() == 1) {
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                    List.of(ExploreEffect.forPermanent(exploringIds.getFirst())));
        } else if (exploringIds.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExploreOrderChoice(entry, List.copyOf(exploringIds)));
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), exploringIds,
                    "Choose which creature explores next.");
        }
    }

    /** Explores the chosen permanent first, then repeats the choice for the rest. */
    public void completeOrderChoice(GameData gameData, UUID chosenId, PermanentChoiceContext.ExploreOrderChoice choice) {
        if (!choice.candidateIds().contains(chosenId)) {
            throw new IllegalStateException("Invalid creature to explore");
        }
        StackEntry entry = choice.resolvingEntry();
        List<UUID> rest = choice.candidateIds().stream().filter(id -> !id.equals(chosenId)).toList();
        List<CardEffect> steps = new ArrayList<>();
        steps.add(ExploreEffect.forPermanent(chosenId));
        if (!rest.isEmpty()) {
            PermanentPredicate remaining = rest.size() == 1
                    ? new PermanentIsSpecificPermanentPredicate(rest.getFirst())
                    : new PermanentAnyOfPredicate(rest.stream()
                    .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
            steps.add(new ExploreEffect(Subjects.controlledBy(Subjects.you(), remaining)));
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
    }

    private void insertEffectsAfter(StackEntry entry, CardEffect effect, List<CardEffect> effects) {
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                entry.insertEffectsToResolve(i + 1, effects);
                return;
            }
        }
    }

    private void insertRemainingExplores(StackEntry entry, CardEffect effect,
                                         ExploreEffect exploreEffect, int exploreCount) {
        if (exploreCount <= 1 || exploreEffect.amount() == null) {
            return;
        }
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                List<CardEffect> remaining = new ArrayList<>(exploreCount - 1);
                for (int j = 1; j < exploreCount; j++) {
                    remaining.add(exploreEffect.permanentId() == null
                            ? new ExploreEffect(exploreEffect.targeted(), exploreEffect.subject(),
                            null, false, null, exploreEffect.targetPredicate())
                            : ExploreEffect.forPermanent(exploreEffect.permanentId()));
                }
                entry.insertEffectsToResolve(i + 1, remaining);
                return;
            }
        }
    }

    private UUID findExploringPermanentId(GameData gameData, StackEntry entry, ExploreEffect effect) {
        if (effect.permanentId() != null) {
            return effect.permanentId();
        }
        if (effect.subject() != null) {
            // A permanent that left still explores, by its last-known information (CR 701.44c).
            List<UUID> ids = subjectResolutionService.permanentIds(
                    gameData, entry, effect, effect.subject());
            return ids.isEmpty() ? null : ids.getFirst();
        }
        return effect.targeted() ? entry.getTargetId() : entry.getSourcePermanentId();
    }
}
