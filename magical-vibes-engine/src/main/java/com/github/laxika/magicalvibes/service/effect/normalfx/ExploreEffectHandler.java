package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.effect.ScryBeforeExploreReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExploreEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExploreEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        ExploreEffect exploreEffect = (ExploreEffect) effect;
        UUID controllerId = entry.getControllerId();
        UUID exploringPermanentId = findExploringPermanentId(gameData, entry, exploreEffect);
        if (exploreEffect.permanentId() != null
                && gameQueryService.findPermanentById(gameData, exploreEffect.permanentId()) == null) {
            return;
        }
        if (exploreEffect.reference() != null && exploringPermanentId == null) {
            return;
        }
        Permanent exploringPermanent = exploringPermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, exploringPermanentId);
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
                            exploreEffect.targeted(), exploreEffect.reference(), exploreEffect.permanentId(),
                            exploreEffect.targetPredicate()));
                }
                insertEffectsAfter(entry, effect, replacementEffects);
                return;
            }
        }
        insertRemainingExplores(entry, effect, exploreEffect, exploreCount);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(playerName + "'s library is empty (" + sourceName + " explores)."));
            putExploreCounter(gameData, entry, exploringPermanent, controllerId);
            gameData.playersWhoControlledPermanentThatExploredThisTurn.add(controllerId);
            triggerCollectionService.checkExploreTriggers(gameData, controllerId, null);
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
            triggerCollectionService.checkExploreTriggers(gameData, controllerId, topCard);
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
            for (CardEffect staticEffect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
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
                            ? new ExploreEffect(exploreEffect.targeted(), exploreEffect.reference(),
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
        if (effect.reference() != null) {
            return switch (effect.reference()) {
                case SOURCE -> entry.getSourcePermanentId();
                case TRIGGERING -> entry.getTriggeringPermanentId();
                case ATTACHED -> {
                    Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    yield source == null ? null : source.getAttachedTo();
                }
                case RETURNED -> findPermanentByCardId(gameData, entry.getTargetId());
            };
        }
        return effect.targeted() ? entry.getTargetId() : entry.getSourcePermanentId();
    }

    private UUID findPermanentByCardId(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())
                        || (permanent.getOriginalCard() != null
                        && cardId.equals(permanent.getOriginalCard().getId()))) {
                    return permanent.getId();
                }
            }
        }
        return null;
    }
}
