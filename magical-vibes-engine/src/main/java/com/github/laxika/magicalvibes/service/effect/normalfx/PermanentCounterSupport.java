package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnPerCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.SagaChapterService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shared permanent-counter helpers used by every migrated counter effect handler and by
 * {@code MultiPermanentChoiceHandlerService} (async proliferate / counter-placement re-entry).
 *
 * <p>Extracted verbatim from the original {@code PermanentCounterResolutionService} monolith;
 * behavior (counter placement, +1/+1 vs -1/-1 annihilation, saga lore chapters) is identical.
 */
@Slf4j
@Component
public class PermanentCounterSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private TriggerCollectionService triggerCollectionService;
    private SagaChapterService sagaChapterService;

    @Autowired
    void setTriggerCollectionService(@Lazy TriggerCollectionService triggerCollectionService) {
        this.triggerCollectionService = triggerCollectionService;
    }

    @Autowired
    void setSagaChapterService(@Lazy SagaChapterService sagaChapterService) {
        this.sagaChapterService = sagaChapterService;
    }

    public void notifyCountersPlaced(GameData gameData, StackEntry entry, Permanent target, int amount) {
        if (triggerCollectionService != null && target != null && amount > 0) {
            triggerCollectionService.checkYouPutCountersTriggers(
                    gameData, placingPlayerId(gameData, entry, target), amount);
        }
    }

    public void notifySelfCountersPlaced(GameData gameData, StackEntry entry, Permanent target,
                                         CounterType counterType, int previousCount, int amount) {
        if (target != null && amount > 0) {
            fireSelfCountersPutTriggers(gameData, target, counterType, previousCount);
        }
    }

    private final ConditionEvaluationService conditionEvaluationService;

    @Autowired
    public PermanentCounterSupport(GameQueryService gameQueryService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   GameLogService gameLogService,
                                   PlayerInputService playerInputService,
                                   ConditionEvaluationService conditionEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    public PermanentCounterSupport(GameQueryService gameQueryService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   GameLogService gameLogService,
                                   PlayerInputService playerInputService) {
        this(gameQueryService, predicateEvaluationService, gameLogService, playerInputService,
                new ConditionEvaluationService(gameQueryService, predicateEvaluationService));
    }

    public void removeCountersAndTransform(GameData gameData, Permanent self, CounterType counterType, String counterName) {
        // Remove all counters of that type
        switch (counterType) {
            case CHARGE -> self.setCounterCount(CounterType.CHARGE, 0);
            case HATCHLING -> self.setCounterCount(CounterType.HATCHLING, 0);
            case LANDMARK -> self.setCounterCount(CounterType.LANDMARK, 0);
            case SLIME -> self.setCounterCount(CounterType.SLIME, 0);
            case STUDY -> self.setCounterCount(CounterType.STUDY, 0);
            case RITUAL -> self.setCounterCount(CounterType.RITUAL, 0);
            case WISH -> self.setCounterCount(CounterType.WISH, 0);
            case BORE -> self.setCounterCount(CounterType.BORE, 0);
            case PLUS_ONE_PLUS_ONE -> self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            case MINUS_ONE_MINUS_ONE -> self.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "All " + counterName + " counters removed from ", self.getCard(), "."));
        log.info("Game {} - All {} counters removed from {}", gameData.id, counterName, self.getCard().getName());

        // Transform
        Card originalCard = self.getOriginalCard();
        if (!self.isTransformed()) {
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null) {
                Card frontCard = self.getCard();
                String frontName = frontCard.getName();
                self.setCard(backFace);
                self.setTransformed(true);
                gameLogService.append(gameData, GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            }
        } else {
            Card backCard = self.getCard();
            String backName = backCard.getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            gameLogService.append(gameData, GameLog.cardTextCard(backCard, " transforms into ", originalCard, "."));
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        }
    }

    public void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters) {
        if (counters <= 0 || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
            return;
        }
        int previousCount = target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        counters = gameQueryService.replaceCounters(gameData, target, CounterType.PLUS_ONE_PLUS_ONE,
                counters, placingPlayerId(gameData, entry, target));
        if (counters <= 0) {
            return;
        }
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
        notifyCountersPlaced(gameData, entry, target, counters);
        notifySelfCountersPlaced(gameData, entry, target, CounterType.PLUS_ONE_PLUS_ONE, previousCount, counters);
        recordCounterPlacedOnCreature(gameData, target, placingPlayerId(gameData, entry, target));
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, counters);

        String counterText = counters == 1 ? "a +1/+1 counter" : counters + " +1/+1 counters";
        gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " gets " + counterText + "."));
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, target.getCard().getName(), counters);

        firePlusOnePlusOneCounterTriggers(gameData, target);
    }

    public void placeCountersOnPermanents(GameData gameData, StackEntry entry, List<UUID> permanentIds, CounterType counterType) {
        List<Card> affectedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !gameQueryService.cantHaveCounters(gameData, perm)) {
                int previousCount = perm.getCounterCount(counterType);
                int placed = gameQueryService.replaceCounters(gameData, perm, counterType, 1,
                        placingPlayerId(gameData, entry, perm));
                if (placed <= 0) {
                    continue;
                }
                switch (counterType) {
                    case AIM -> perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + placed);
                    case CHARGE -> perm.setCounterCount(CounterType.CHARGE, perm.getCounterCount(CounterType.CHARGE) + placed);
                    case HOUR -> perm.setCounterCount(CounterType.HOUR, perm.getCounterCount(CounterType.HOUR) + placed);
                    case LEVEL -> perm.setCounterCount(CounterType.LEVEL, perm.getCounterCount(CounterType.LEVEL) + placed);
                    case RITUAL -> perm.setCounterCount(CounterType.RITUAL, perm.getCounterCount(CounterType.RITUAL) + placed);
                    case DEATHTOUCH, DECAYED, FLYING, FIRST_STRIKE, DOUBLE_STRIKE, HEXPROOF, INDESTRUCTIBLE, LIFELINK,
                         REACH, TRAMPLE -> {
                        perm.setCounterCount(counterType, perm.getCounterCount(counterType) + placed);
                        perm.setCounterTimestamp(counterType, gameData.nextTimestamp());
                    }
                    default -> throw new IllegalArgumentException("Unsupported counter type for placement: " + counterType);
                }
                recordCounterPlacedOnCreature(gameData, perm, placingPlayerId(gameData, entry, perm));
                notifyCountersPlaced(gameData, entry, perm, placed);
                notifySelfCountersPlaced(gameData, entry, perm, counterType, previousCount, placed);
                fireCounterPutOnControlledCreatureTriggers(gameData, perm, placed);
                affectedCards.add(perm.getCard());
            }
        }

        if (!affectedCards.isEmpty()) {
            String counterName = counterType.name().toLowerCase();
            GameLog.Builder builder = GameLog.builder()
                    .card(entry.getCard())
                    .text(" puts an " + counterName + " counter on ");
            for (int i = 0; i < affectedCards.size(); i++) {
                if (i > 0) {
                    builder.text(", ");
                }
                builder.card(affectedCards.get(i));
            }
            builder.text(".");
            gameLogService.append(gameData, builder.build());
            log.info("Game {} - {} places {} counters on {} permanents", gameData.id,
                    entry.getCard().getName(), counterName, affectedCards.size());
        }
    }

    public void resolveCounterOnOwnPermanent(GameData gameData, StackEntry entry,
                                            CounterType counterType, int count, PermanentPredicate predicate) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId);

        List<UUID> eligibleIds = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(p, predicate, filterContext)) {
                eligibleIds.add(p.getId());
            }
        }

        if (eligibleIds.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": no eligible permanent to put counters on."));
            log.info("Game {} - {} no eligible permanent for counter placement", gameData.id, entry.getCard().getName());
            return;
        }

        if (eligibleIds.size() == 1) {
            Permanent target = gameQueryService.findPermanentById(gameData, eligibleIds.getFirst());
            if (target != null && !gameQueryService.cantHaveCounters(gameData, target)) {
                placeCounterOnPermanent(gameData, entry, target, counterType, count);
            }
        } else {
            // Multiple eligible — controller must choose one
            String counterName = counterTypeName(counterType);
            playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligibleIds,
                    1, new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(counterType, count),
                    "Choose a permanent to put " + count + " " + counterName + " counter(s) on.");
        }
    }

    public int placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                       CounterType counterType, int count) {
        if (gameQueryService.cantHaveCounters(gameData, target)) return 0;

        int previousLoreCount = counterType == CounterType.LORE
                ? target.getCounterCount(CounterType.LORE) : 0;
        int previousCount = target.getCounterCount(counterType);
        count = gameQueryService.replaceCounters(gameData, target, counterType, count,
                placingPlayerId(gameData, entry, target));

        String counterName = switch (counterType) {
            case CHARGE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case LEVEL -> { target.setCounterCount(CounterType.LEVEL, target.getCounterCount(CounterType.LEVEL) + count); yield "level"; }
            case LORE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.LORE, target.getCounterCount(CounterType.LORE) + 1); yield "lore"; }
            case LOYALTY -> { target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) + count); yield "loyalty"; }
            case LUCK -> { target.setCounterCount(CounterType.LUCK, target.getCounterCount(CounterType.LUCK) + count); yield "luck"; }
            case OIL -> { target.setCounterCount(CounterType.OIL, target.getCounterCount(CounterType.OIL) + count); yield "oil"; }
            case PLUS_ONE_PLUS_ONE -> {
                if (count <= 0 || gameQueryService.cantHavePlusOnePlusOneCounters(gameData, target)) {
                    yield null;
                }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
                firePlusOnePlusOneCounterTriggers(gameData, target);
                yield "+1/+1";
            }
            case PLUS_ONE_PLUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_ZERO, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ZERO) + count);
                yield "+1/+0";
            }
            case PLUS_TWO_PLUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_TWO_PLUS_ZERO, target.getCounterCount(CounterType.PLUS_TWO_PLUS_ZERO) + count);
                yield "+2/+0";
            }
            case PLUS_ONE_PLUS_TWO -> {
                if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_TWO, target.getCounterCount(CounterType.PLUS_ONE_PLUS_TWO) + count);
                yield "+1/+2";
            }
            case PLUS_TWO_PLUS_TWO -> {
                if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.PLUS_TWO_PLUS_TWO, target.getCounterCount(CounterType.PLUS_TWO_PLUS_TWO) + count);
                yield "+2/+2";
            }
            case MINUS_ONE_MINUS_ONE -> {
                if (count <= 0 || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);
                yield "-1/-1";
            }
            case MINUS_ONE_MINUS_ZERO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ZERO, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ZERO) + count);
                yield "-1/-0";
            }
            case MINUS_TWO_MINUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_TWO_MINUS_ONE, target.getCounterCount(CounterType.MINUS_TWO_MINUS_ONE) + count);
                yield "-2/-1";
            }
            case MINUS_TWO_MINUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_TWO_MINUS_TWO, target.getCounterCount(CounterType.MINUS_TWO_MINUS_TWO) + count);
                yield "-2/-2";
            }
            case MINUS_ZERO_MINUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ZERO_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ZERO_MINUS_ONE) + count);
                yield "-0/-1";
            }
            case MINUS_ZERO_MINUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ZERO_MINUS_TWO, target.getCounterCount(CounterType.MINUS_ZERO_MINUS_TWO) + count);
                yield "-0/-2";
            }
            case PLUS_ZERO_PLUS_ONE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ZERO_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE) + count);
                yield "+0/+1";
            }
            case PLUS_ZERO_PLUS_TWO -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.PLUS_ZERO_PLUS_TWO, target.getCounterCount(CounterType.PLUS_ZERO_PLUS_TWO) + count);
                yield "+0/+2";
            }
            case CARRION -> { target.setCounterCount(CounterType.CARRION, target.getCounterCount(CounterType.CARRION) + count); yield "carrion"; }
            case CUBE -> { target.setCounterCount(CounterType.CUBE, target.getCounterCount(CounterType.CUBE) + count); yield "cube"; }
            case CREDIT -> { target.setCounterCount(CounterType.CREDIT, target.getCounterCount(CounterType.CREDIT) + count); yield "credit"; }
            case CURRENCY -> { target.setCounterCount(CounterType.CURRENCY, target.getCounterCount(CounterType.CURRENCY) + count); yield "currency"; }
            case DOOM -> { target.setCounterCount(CounterType.DOOM, target.getCounterCount(CounterType.DOOM) + count); yield "doom"; }
            case CORPSE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.CORPSE, target.getCounterCount(CounterType.CORPSE) + count);
                yield "corpse";
            }
            case DESPAIR -> { target.setCounterCount(CounterType.DESPAIR, target.getCounterCount(CounterType.DESPAIR) + count); yield "despair"; }
            case DEATH -> { target.setCounterCount(CounterType.DEATH, target.getCounterCount(CounterType.DEATH) + count); yield "death"; }
            case DELAY -> { target.setCounterCount(CounterType.DELAY, target.getCounterCount(CounterType.DELAY) + count); yield "delay"; }
            case DEVOTION -> { target.setCounterCount(CounterType.DEVOTION, target.getCounterCount(CounterType.DEVOTION) + count); yield "devotion"; }
            case DIVINITY -> { target.setCounterCount(CounterType.DIVINITY, target.getCounterCount(CounterType.DIVINITY) + count); yield "divinity"; }
            case HATCHLING -> { target.setCounterCount(CounterType.HATCHLING, target.getCounterCount(CounterType.HATCHLING) + count); yield "hatchling"; }
            case HOOFPRINT -> { target.setCounterCount(CounterType.HOOFPRINT, target.getCounterCount(CounterType.HOOFPRINT) + count); yield "hoofprint"; }
            case HUNGER -> { target.setCounterCount(CounterType.HUNGER, target.getCounterCount(CounterType.HUNGER) + count); yield "hunger"; }
            case INVITATION -> { target.setCounterCount(CounterType.INVITATION, target.getCounterCount(CounterType.INVITATION) + count); yield "invitation"; }
            case UNITY -> { target.setCounterCount(CounterType.UNITY, target.getCounterCount(CounterType.UNITY) + count); yield "unity"; }
            case KI -> { target.setCounterCount(CounterType.KI, target.getCounterCount(CounterType.KI) + count); yield "ki"; }
            case LANDMARK -> { target.setCounterCount(CounterType.LANDMARK, target.getCounterCount(CounterType.LANDMARK) + count); yield "landmark"; }
            case RITUAL -> { target.setCounterCount(CounterType.RITUAL, target.getCounterCount(CounterType.RITUAL) + count); yield "ritual"; }
            case STUDY -> { target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) + count); yield "study"; }
            case WISH -> { target.setCounterCount(CounterType.WISH, target.getCounterCount(CounterType.WISH) + count); yield "wish"; }
            case SLEIGHT -> { target.setCounterCount(CounterType.SLEIGHT, target.getCounterCount(CounterType.SLEIGHT) + count); yield "sleight"; }
            case SLIME -> { target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) + count); yield "slime"; }
            case STORAGE -> { target.setCounterCount(CounterType.STORAGE, target.getCounterCount(CounterType.STORAGE) + count); yield "storage"; }
            case AIM -> { target.setCounterCount(CounterType.AIM, target.getCounterCount(CounterType.AIM) + count); yield "aim"; }
            case ARROW -> { target.setCounterCount(CounterType.ARROW, target.getCounterCount(CounterType.ARROW) + count); yield "arrow"; }
            case BLAZE -> { target.setCounterCount(CounterType.BLAZE, target.getCounterCount(CounterType.BLAZE) + count); yield "blaze"; }
            case BLIGHT -> { target.setCounterCount(CounterType.BLIGHT, target.getCounterCount(CounterType.BLIGHT) + count); yield "blight"; }
            case BLOOD -> { target.setCounterCount(CounterType.BLOOD, target.getCounterCount(CounterType.BLOOD) + count); yield "blood"; }
            case BOUNTY -> { target.setCounterCount(CounterType.BOUNTY, target.getCounterCount(CounterType.BOUNTY) + count); yield "bounty"; }
            case BRIBERY -> { target.setCounterCount(CounterType.BRIBERY, target.getCounterCount(CounterType.BRIBERY) + count); yield "bribery"; }
            case BRICK -> { target.setCounterCount(CounterType.BRICK, target.getCounterCount(CounterType.BRICK) + count); yield "brick"; }
            case GEM -> { target.setCounterCount(CounterType.GEM, target.getCounterCount(CounterType.GEM) + count); yield "gem"; }
            case ELIXIR -> { target.setCounterCount(CounterType.ELIXIR, target.getCounterCount(CounterType.ELIXIR) + count); yield "elixir"; }
            case EON -> { target.setCounterCount(CounterType.EON, target.getCounterCount(CounterType.EON) + count); yield "eon"; }
            case EYEBALL -> { target.setCounterCount(CounterType.EYEBALL, target.getCounterCount(CounterType.EYEBALL) + count); yield "eyeball"; }
            case FADE -> { target.setCounterCount(CounterType.FADE, target.getCounterCount(CounterType.FADE) + count); yield "fade"; }
            case GOLD -> { target.setCounterCount(CounterType.GOLD, target.getCounterCount(CounterType.GOLD) + count); yield "gold"; }
            case GHOSTFORM -> { target.setCounterCount(CounterType.GHOSTFORM, target.getCounterCount(CounterType.GHOSTFORM) + count); yield "ghostform"; }
            case GROWTH -> { target.setCounterCount(CounterType.GROWTH, target.getCounterCount(CounterType.GROWTH) + count); yield "growth"; }
            case PRESSURE -> { target.setCounterCount(CounterType.PRESSURE, target.getCounterCount(CounterType.PRESSURE) + count); yield "pressure"; }
            case POLYP -> { target.setCounterCount(CounterType.POLYP, target.getCounterCount(CounterType.POLYP) + count); yield "polyp"; }
            case PLAGUE -> { target.setCounterCount(CounterType.PLAGUE, target.getCounterCount(CounterType.PLAGUE) + count); yield "plague"; }
            case QUEST -> { target.setCounterCount(CounterType.QUEST, target.getCounterCount(CounterType.QUEST) + count); yield "quest"; }
            case PUPA -> { target.setCounterCount(CounterType.PUPA, target.getCounterCount(CounterType.PUPA) + count); yield "pupa"; }
            case PAGE -> { target.setCounterCount(CounterType.PAGE, target.getCounterCount(CounterType.PAGE) + count); yield "page"; }
            case STUN -> { target.setCounterCount(CounterType.STUN, target.getCounterCount(CounterType.STUN) + count); yield "stun"; }
            case TOWER -> { target.setCounterCount(CounterType.TOWER, target.getCounterCount(CounterType.TOWER) + count); yield "tower"; }
            case TIME -> { target.setCounterCount(CounterType.TIME, target.getCounterCount(CounterType.TIME) + count); yield "time"; }
            case TREASURE -> { target.setCounterCount(CounterType.TREASURE, target.getCounterCount(CounterType.TREASURE) + count); yield "treasure"; }
            case AGE -> { target.setCounterCount(CounterType.AGE, target.getCounterCount(CounterType.AGE) + count); yield "age"; }
            case BAIT -> { target.setCounterCount(CounterType.BAIT, target.getCounterCount(CounterType.BAIT) + count); yield "bait"; }
            case VITALITY -> { target.setCounterCount(CounterType.VITALITY, target.getCounterCount(CounterType.VITALITY) + count); yield "vitality"; }
            case VALOR -> { target.setCounterCount(CounterType.VALOR, target.getCounterCount(CounterType.VALOR) + count); yield "valor"; }
            case HEALING -> { target.setCounterCount(CounterType.HEALING, target.getCounterCount(CounterType.HEALING) + count); yield "healing"; }
            case FEATHER -> { target.setCounterCount(CounterType.FEATHER, target.getCounterCount(CounterType.FEATHER) + count); yield "feather"; }
            case FATE -> { target.setCounterCount(CounterType.FATE, target.getCounterCount(CounterType.FATE) + count); yield "fate"; }
            case FILIBUSTER -> { target.setCounterCount(CounterType.FILIBUSTER, target.getCounterCount(CounterType.FILIBUSTER) + count); yield "filibuster"; }
            case FLOOD -> { target.setCounterCount(CounterType.FLOOD, target.getCounterCount(CounterType.FLOOD) + count); yield "flood"; }
            case HOURGLASS -> { target.setCounterCount(CounterType.HOURGLASS, target.getCounterCount(CounterType.HOURGLASS) + count); yield "hourglass"; }
            case PAIN -> { target.setCounterCount(CounterType.PAIN, target.getCounterCount(CounterType.PAIN) + count); yield "pain"; }
            case PARALYZATION -> { target.setCounterCount(CounterType.PARALYZATION, target.getCounterCount(CounterType.PARALYZATION) + count); yield "paralyzation"; }
            case PETAL -> { target.setCounterCount(CounterType.PETAL, target.getCounterCount(CounterType.PETAL) + count); yield "petal"; }
            case PETRIFICATION -> { target.setCounterCount(CounterType.PETRIFICATION, target.getCounterCount(CounterType.PETRIFICATION) + count); yield "petrification"; }
            case PIN -> { target.setCounterCount(CounterType.PIN, target.getCounterCount(CounterType.PIN) + count); yield "pin"; }
            case PREY -> { target.setCounterCount(CounterType.PREY, target.getCounterCount(CounterType.PREY) + count); yield "prey"; }
            case FUNGUS -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.FUNGUS, target.getCounterCount(CounterType.FUNGUS) + count);
                yield "fungus";
            }
            case FUSE -> { target.setCounterCount(CounterType.FUSE, target.getCounterCount(CounterType.FUSE) + count); yield "fuse"; }
            case VERSE -> { target.setCounterCount(CounterType.VERSE, target.getCounterCount(CounterType.VERSE) + count); yield "verse"; }
            case ICE -> { target.setCounterCount(CounterType.ICE, target.getCounterCount(CounterType.ICE) + count); yield "ice"; }
            case INFECTION -> { target.setCounterCount(CounterType.INFECTION, target.getCounterCount(CounterType.INFECTION) + count); yield "infection"; }
            case INCARNATION -> { target.setCounterCount(CounterType.INCARNATION, target.getCounterCount(CounterType.INCARNATION) + count); yield "incarnation"; }
            case INCUBATION -> { target.setCounterCount(CounterType.INCUBATION, target.getCounterCount(CounterType.INCUBATION) + count); yield "incubation"; }
            case MAGNET -> { target.setCounterCount(CounterType.MAGNET, target.getCounterCount(CounterType.MAGNET) + count); yield "magnet"; }
            case MANIFESTATION -> { target.setCounterCount(CounterType.MANIFESTATION, target.getCounterCount(CounterType.MANIFESTATION) + count); yield "manifestation"; }
            case MINE -> { target.setCounterCount(CounterType.MINE, target.getCounterCount(CounterType.MINE) + count); yield "mine"; }
            case MUSIC -> { target.setCounterCount(CounterType.MUSIC, target.getCounterCount(CounterType.MUSIC) + count); yield "music"; }
            case MUSTER -> { target.setCounterCount(CounterType.MUSTER, target.getCounterCount(CounterType.MUSTER) + count); yield "muster"; }
            case NIGHT -> { target.setCounterCount(CounterType.NIGHT, target.getCounterCount(CounterType.NIGHT) + count); yield "night"; }
            case WIND -> { target.setCounterCount(CounterType.WIND, target.getCounterCount(CounterType.WIND) + count); yield "wind"; }
            case WINCH -> { target.setCounterCount(CounterType.WINCH, target.getCounterCount(CounterType.WINCH) + count); yield "winch"; }
            case WAGE -> { target.setCounterCount(CounterType.WAGE, target.getCounterCount(CounterType.WAGE) + count); yield "wage"; }
            case RUST -> { target.setCounterCount(CounterType.RUST, target.getCounterCount(CounterType.RUST) + count); yield "rust"; }
            case SOOT -> { target.setCounterCount(CounterType.SOOT, target.getCounterCount(CounterType.SOOT) + count); yield "soot"; }
            case SOUL -> { target.setCounterCount(CounterType.SOUL, target.getCounterCount(CounterType.SOUL) + count); yield "soul"; }
            case VORTEX -> { target.setCounterCount(CounterType.VORTEX, target.getCounterCount(CounterType.VORTEX) + count); yield "vortex"; }
            case VELOCITY -> { target.setCounterCount(CounterType.VELOCITY, target.getCounterCount(CounterType.VELOCITY) + count); yield "velocity"; }
            case TRAINING -> { target.setCounterCount(CounterType.TRAINING, target.getCounterCount(CounterType.TRAINING) + count); yield "training"; }
            case THEFT -> { target.setCounterCount(CounterType.THEFT, target.getCounterCount(CounterType.THEFT) + count); yield "theft"; }
            case TIDE -> { target.setCounterCount(CounterType.TIDE, target.getCounterCount(CounterType.TIDE) + count); yield "tide"; }
            case DEATHTOUCH, DECAYED, FLYING, FIRST_STRIKE, DOUBLE_STRIKE, HEXPROOF, INDESTRUCTIBLE, LIFELINK,
                 REACH, TRAMPLE -> {
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                if (count > 0) {
                    target.setCounterTimestamp(counterType, gameData.nextTimestamp());
                }
                yield counterType.name().toLowerCase();
            }
            default -> {
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                yield counterType.name().toLowerCase();
            }
        };
        if (counterName == null || count <= 0) return 0;

        notifyCountersPlaced(gameData, entry, target, count);
        notifySelfCountersPlaced(gameData, entry, target, counterType, previousCount, count);
        recordCounterPlacedOnCreature(gameData, target, placingPlayerId(gameData, entry, target));
        if (counterType == CounterType.PLUS_ONE_PLUS_ONE) {
            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, count);
        }

        Card card = target.getCard();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        Card sourceCard = entry != null ? entry.getCard() : card;
        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " puts " + counterText + " on ", card, "."));
        log.info("Game {} - {} puts {} {} counter(s) on {}", gameData.id,
                sourceCard.getName(), count, counterName, card.getName());

        // Lore counters on Sagas trigger each chapter crossed by the placement.
        if (counterType == CounterType.LOYALTY) {
            fireLoyaltyCountersPutOnPlaneswalkerTriggers(gameData, target, count);
        }

        // Lore counters on Sagas trigger chapter abilities (MTG Rule 714.3b)
        if (entry != null && counterType == CounterType.LORE && card.isSaga()) {
            int finalLoreCount = target.getCounterCount(CounterType.LORE);
            for (int loreCount = previousLoreCount + 1; loreCount <= finalLoreCount; loreCount++) {
                triggerSagaChapter(gameData, entry, target, loreCount);
            }
        }

        // Flourishing Defenses etc.: "whenever a -1/-1 counter is put on a creature." The placing player
        // is the resolving spell/ability's controller — read it from the entry rather than
        // currentlyResolvingControllerId, which is null when resolution was resumed asynchronously after a
        // target choice (e.g. Hapatra's combat-damage "put a -1/-1 counter on target creature").
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE) {
            UUID placingPlayerId = entry != null ? entry.getControllerId() : gameData.currentlyResolvingControllerId;
            fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, count, placingPlayerId);
        } else if (counterType != CounterType.PLUS_ONE_PLUS_ONE) {
            fireCounterPutOnControlledCreatureTriggers(gameData, target, count);
        }
        return count;
    }

    public String counterTypeName(CounterType counterType) {
        return switch (counterType) {
            case CHARGE -> "charge";
            case LORE -> "lore";
            case LOYALTY -> "loyalty";
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case PLUS_ONE_PLUS_ZERO -> "+1/+0";
            case PLUS_TWO_PLUS_ZERO -> "+2/+0";
            case PLUS_ONE_PLUS_TWO -> "+1/+2";
            case PLUS_TWO_PLUS_TWO -> "+2/+2";
            case PLUS_ZERO_PLUS_TWO -> "+0/+2";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case MINUS_ONE_MINUS_ZERO -> "-1/-0";
            case MINUS_TWO_MINUS_TWO -> "-2/-2";
            case HATCHLING -> "hatchling";
            case STUDY -> "study";
            case WISH -> "wish";
            case SLIME -> "slime";
            case AIM -> "aim";
            case DELAY -> "delay";
            default -> counterType.name().toLowerCase();
        };
    }

    public void removeCounterFromPermanent(GameData gameData, Permanent target,
                                            CounterType counterType, int amount) {
        int current = target.getCounterCount(counterType);
        if (current <= 0 || amount <= 0) {
            return;
        }

        int removed = Math.min(current, amount);
        target.setCounterCount(counterType, current - removed);
        if (counterType == CounterType.OIL) {
            gameData.recordOilCounterRemoved(target, removed);
        }
        gameLogService.append(gameData, GameLog.builder()
                .card(target.getCard())
                .text(" removes " + removed + " " + counterTypeName(counterType) + " counter(s).")
                .build());
        log.info("Game {} - {} removes {} {} counter(s)", gameData.id, target.getCard().getName(),
                removed, counterTypeName(counterType));
    }

    public void removeCountersFromPermanent(GameData gameData, Permanent permanent,
                                            CounterType counterType, int count) {
        int remaining = count;
        if (counterType == CounterType.ANY) {
            int minusOneCounters = permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
            int removeMinusOne = Math.min(minusOneCounters, remaining);
            permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, minusOneCounters - removeMinusOne);
            remaining -= removeMinusOne;
            if (remaining > 0) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - remaining);
            }
        } else {
            int current = permanent.getCounterCount(counterType);
            int removed = Math.min(current, count);
            permanent.setCounterCount(counterType, current - removed);
            if (counterType == CounterType.OIL) {
                gameData.recordOilCounterRemoved(permanent, removed);
            }
        }

        String counterName = counterTypeName(counterType);
        String counterText = count == 1
                ? "a " + counterName + " counter"
                : count + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.cardThen(
                permanent.getCard(), " removes " + counterText + "."));
    }

    private void triggerSagaChapter(GameData gameData, StackEntry entry, Permanent saga, int loreCount) {
        Card card = saga.getCard();

        if (sagaChapterService != null) {
            sagaChapterService.triggerSagaChapter(gameData, saga, card, entry.getControllerId(), loreCount);
            return;
        }

        EffectSlot chapterSlot = switch (loreCount) {
            case 1 -> EffectSlot.SAGA_CHAPTER_I;
            case 2 -> EffectSlot.SAGA_CHAPTER_II;
            case 3 -> EffectSlot.SAGA_CHAPTER_III;
            case 4 -> EffectSlot.SAGA_CHAPTER_IV;
            case 5 -> EffectSlot.SAGA_CHAPTER_V;
            default -> null;
        };
        if (chapterSlot == null) return;

        List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
        if (chapterEffects.isEmpty()) return;

        String chapterName = switch (loreCount) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(loreCount);
        };

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                entry.getControllerId(),
                card.getName() + "'s chapter " + chapterName + " ability",
                new ArrayList<>(chapterEffects),
                null,
                saga.getId()
        ));

        gameLogService.append(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
        log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
    }

    /**
     * Convenience overload that infers the placing player from
     * {@link GameData#currentlyResolvingControllerId} — correct for every counter placed while a spell
     * or ability resolves (target/mass/source counter effects). Combat callers that place counters
     * outside stack resolution (wither/infect damage, end-of-combat self counters) must use the
     * {@code placingPlayerId} overload so the controller-restricted watcher fires correctly.
     */
    public void fireMinusOneMinusOneCounterPutOnCreatureTriggers(GameData gameData, Permanent creature, int count) {
        fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, creature, count, gameData.currentlyResolvingControllerId);
    }

    /**
     * Fires the "whenever a -1/-1 counter is put on a creature" watchers once for each of the
     * {@code count} -1/-1 counters just placed on {@code creature}. Every permanent on any battlefield
     * carrying {@link EffectSlot#ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE} (the global watcher,
     * Flourishing Defenses) triggers under its own controller. Permanents carrying
     * {@link EffectSlot#ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE} (Nest of Scarabs) trigger
     * only when their controller equals {@code placingPlayerId} — i.e. only when that player is the one
     * putting the counters. Per the Gatherer ruling that ability triggers once for each individual
     * counter, so a separate trigger is pushed per counter.
     *
     * <p>Permanents carrying {@link EffectSlot#ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE}
     * (Hapatra, Vizier of Poisons) are the "one or more counters, do it once" variant: they also trigger
     * only when their controller equals {@code placingPlayerId}, but fire exactly once for this creature
     * regardless of {@code count}. No-op unless {@code creature} is a creature.</p>
     */
    public void fireMinusOneMinusOneCounterPutOnCreatureTriggers(GameData gameData, Permanent creature, int count, UUID placingPlayerId) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        recordCounterPlacedOnCreature(gameData, creature, placingPlayerId);
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            boolean placedByThisController = controllerId.equals(placingPlayerId);
            for (Permanent source : new ArrayList<>(battlefield)) {
                Card card = source.getCard();

                // Per-counter watchers: global (Flourishing Defenses) + you-put (Nest of Scarabs).
                List<CardEffect> globalEffects = card.getEffects(EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE);
                List<CardEffect> youPutEffects = placedByThisController
                        ? card.getEffects(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE)
                        : List.of();
                List<CardEffect> perCounterEffects = globalEffects.isEmpty() ? youPutEffects
                        : youPutEffects.isEmpty() ? globalEffects
                        : concat(globalEffects, youPutEffects);
                if (!perCounterEffects.isEmpty()) {
                    for (int i = 0; i < count; i++) {
                        gameData.stack.add(new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                card,
                                controllerId,
                                card.getName() + "'s triggered ability",
                                new ArrayList<>(perCounterEffects),
                                null,
                                source.getId()
                        ));
                        gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
                    }
                    log.info("Game {} - {} -1/-1-counter watcher fires {} time(s)", gameData.id, card.getName(), count);
                }

                // Once-per-creature you-put watcher (Hapatra): fires a single trigger regardless of count.
                List<CardEffect> youPutOnceEffects = placedByThisController
                        ? card.getEffects(EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE)
                        : List.of();
                if (!youPutOnceEffects.isEmpty()) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s triggered ability",
                            new ArrayList<>(youPutOnceEffects),
                            null,
                            source.getId()
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
                    log.info("Game {} - {} once-per-creature -1/-1-counter watcher fires", gameData.id, card.getName());
                }
            }
        });

        // Self-scoped "Whenever you put one or more -1/-1 counters on this creature" (Defiant Greatmaw):
        // fires once per event (not per counter), only when the controller is the placing player.
        fireSelfMinusOneMinusOneCountersPutTriggers(gameData, creature, placingPlayerId);
        fireCounterPutOnControlledCreatureTriggers(gameData, creature, count);
    }

    /**
     * Fires {@link EffectSlot#ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT} on {@code creature} — the -1/-1
     * mirror of {@link #firePlusOnePlusOneCountersPutOnSelfTriggers}. Fires once per placement event
     * (regardless of the counter count) and only when {@code placingPlayerId} is the creature's own
     * controller ("Whenever you put …"). A targeted effect in the slot has its target chosen as the
     * ability goes on the stack, reusing the {@code SpellTargetTriggerAnyTarget} interaction; the effect
     * declares its legal targets through its {@code targetSpec()} predicate.
     */
    void fireSelfMinusOneMinusOneCountersPutTriggers(GameData gameData, Permanent creature, UUID placingPlayerId) {
        Card card = creature.getCard();
        List<CardEffect> effects = card.getEffects(EffectSlot.ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT);
        if (effects.isEmpty()) {
            return;
        }

        UUID controllerId = controllerOf(gameData, creature);
        if (controllerId == null || !controllerId.equals(placingPlayerId)) {
            return;
        }

        boolean needsTarget = effects.stream().anyMatch(e -> e.targetSpec().declaredTarget() != null);
        if (needsTarget) {
            PermanentPredicate targetPredicate = effects.stream()
                    .map(e -> e.targetSpec().predicate())
                    .filter(Objects::nonNull)
                    .findFirst().orElse(null);
            TargetFilter targetFilter = targetPredicate != null
                    ? new PermanentPredicateTargetFilter(targetPredicate, "Choose a target.")
                    : null;
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    card, controllerId, new ArrayList<>(effects), false, targetFilter));
            gameLogService.append(gameData,
                    GameLog.cardThen(card, "'s triggered ability triggers — choose a target."));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + "'s triggered ability",
                    new ArrayList<>(effects), null, creature.getId()));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        }
        log.info("Game {} - {} self -1/-1-counter trigger fires", gameData.id, card.getName());
    }

    private UUID controllerOf(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    private static List<CardEffect> concat(List<CardEffect> a, List<CardEffect> b) {
        List<CardEffect> merged = new ArrayList<>(a);
        merged.addAll(b);
        return merged;
    }

    public void firePlusOnePlusOneCounterTriggers(GameData gameData, Permanent target) {
        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target);
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(gameData, target);
        fireCounterPutOnControlledCreatureTriggers(gameData, target, 1);
    }

    /** Fires generic counter-placement watchers on the creature's controller's battlefield. */
    public void fireCounterPutOnControlledCreatureTriggers(GameData gameData, Permanent creature, int count) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }

        for (Permanent source : new ArrayList<>(battlefield)) {
            Card card = source.getCard();
            List<CardEffect> effects = card.getEffects(EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE);
            if (effects.isEmpty()) {
                continue;
            }
            for (int i = 0; i < count; i++) {
                List<CardEffect> effectsToResolve = new ArrayList<>();
                boolean oncePerTurnQueued = false;
                boolean oncePerCreatureQueued = false;
                for (CardEffect effect : effects) {
                    CardEffect resolved = effect;
                    if (resolved instanceof ConditionalEffect conditional && conditional.interveningIf()) {
                        if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                                ConditionContext.forPermanent(source, controllerId))) {
                            continue;
                        }
                        resolved = conditional.wrapped();
                    }
                    if (resolved instanceof OncePerTurnTriggerEffect oncePerTurn) {
                        if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) {
                            continue;
                        }
                        resolved = oncePerTurn.wrapped();
                        oncePerTurnQueued = true;
                    }
                    if (resolved instanceof OncePerTurnPerCreatureTriggerEffect oncePerCreature) {
                        Set<UUID> firedCreatures = gameData.oncePerCreatureTriggersFiredThisTurn
                                .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet());
                        if (firedCreatures.contains(creature.getId())) {
                            continue;
                        }
                        resolved = oncePerCreature.wrapped();
                        oncePerCreatureQueued = true;
                    }
                    effectsToResolve.add(resolved);
                }
                if (effectsToResolve.isEmpty()) {
                    continue;
                }
                if (oncePerTurnQueued) {
                    gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
                }
                if (oncePerCreatureQueued) {
                    gameData.oncePerCreatureTriggersFiredThisTurn
                            .computeIfAbsent(source.getId(), ignored -> ConcurrentHashMap.newKeySet())
                            .add(creature.getId());
                }
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s triggered ability",
                        effectsToResolve,
                        null,
                        source.getId()
                );
                trigger.setTriggeringPermanentId(creature.getId());
                trigger.setTriggeringPermanentControllerId(controllerId);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
                log.info("Game {} - {} generic counter watcher fires", gameData.id, card.getName());
            }
        }
    }

    /**
     * Fires controller-scoped triggers for loyalty counters placed on planeswalkers. The amount is
     * snapshotted on the triggered ability so "that many" remains the amount from the placement
     * event when the trigger resolves.
     */
    public void fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(
            GameData gameData, UUID controllerId, int count) {
        if (controllerId == null || count <= 0) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            queueLoyaltyCounterTrigger(gameData, source, controllerId, count);
        }
    }

    public void fireLoyaltyCountersPutOnSourceTrigger(GameData gameData, Permanent source, int count) {
        if (source == null || count <= 0) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, source.getId());
        queueLoyaltyCounterTrigger(gameData, source, controllerId, count);
    }

    public void fireLoyaltyCountersPutOnPlaneswalkerTriggers(
            GameData gameData, Permanent planeswalker, int count) {
        if (planeswalker == null || count <= 0 || !gameQueryService.isPlaneswalker(gameData, planeswalker)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, planeswalker.getId());
        fireLoyaltyCountersPutOnControlledPlaneswalkersTriggers(gameData, controllerId, count);
    }

    private void queueLoyaltyCounterTrigger(GameData gameData, Permanent source,
                                            UUID controllerId, int count) {
        if (controllerId == null) {
            return;
        }
        Card card = source.getCard();
        List<CardEffect> effects = card.getEffects(
                EffectSlot.ON_YOU_PUT_LOYALTY_COUNTERS_ON_PLANESWALKERS);
        if (effects.isEmpty()) {
            return;
        }

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                source.getId());
        trigger.setEventValue(count);
        gameData.stack.add(trigger);
        gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        log.info("Game {} - {} loyalty-counter trigger fires for {} counter(s)",
                gameData.id, card.getName(), count);
    }

    void firePlusOnePlusOneCountersPutOnSelfTriggers(GameData gameData, Permanent target) {
        Card card = target.getCard();
        List<CardEffect> effects = card.getEffects(EffectSlot.ON_SELF_PLUS_ONE_PLUS_ONE_COUNTERS_PUT);
        if (effects.isEmpty()) {
            return;
        }

        UUID controllerId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield != null && battlefield.contains(target)) {
                controllerId = playerId;
                break;
            }
        }
        if (controllerId == null) {
            return;
        }

        List<CardEffect> effectsToResolve = new ArrayList<>();
        boolean oncePerTurnQueued = false;
        for (CardEffect effect : effects) {
            if (effect instanceof OncePerTurnTriggerEffect once) {
                if (gameData.oncePerTurnTriggersFiredThisTurn.contains(target.getId())) {
                    continue;
                }
                effectsToResolve.add(once.wrapped());
                oncePerTurnQueued = true;
            } else {
                effectsToResolve.add(effect);
            }
        }
        if (effectsToResolve.isEmpty()) {
            return;
        }
        if (oncePerTurnQueued) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(target.getId());
        }

        boolean needsTarget = effectsToResolve.stream()
                .anyMatch(effect -> effect.targetSpec().declaredTarget() != null);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    card, controllerId, effectsToResolve,
                    "+1/+1 counter placement", target.getId()));
            gameLogService.append(gameData,
                    GameLog.cardThen(card, "'s triggered ability triggers — choose a target."));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s triggered ability",
                    effectsToResolve,
                    null,
                    target.getId()
            ));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        }
        log.info("Game {} - {} +1/+1 counter trigger fires", gameData.id, card.getName());
    }

    private void fireSelfCountersPutTriggers(GameData gameData, Permanent target,
                                              CounterType counterType, int previousCount) {
        Card card = target.getCard();
        List<CardEffect> effects = card.getEffects(EffectSlot.ON_SELF_COUNTERS_PUT);
        if (effects.isEmpty()) {
            return;
        }

        UUID controllerId = controllerOf(gameData, target);
        if (controllerId == null) {
            return;
        }

        int currentCount = target.getCounterCount(counterType);
        List<CardEffect> effectsToResolve = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (effect instanceof ConditionalEffect conditional) {
                if (conditional.condition() instanceof SourceCounterThreshold threshold) {
                    if (conditional.interveningIf()
                            && (threshold.counterType() != counterType
                            || previousCount >= threshold.threshold()
                            || currentCount < threshold.threshold())) {
                        continue;
                    }
                } else if (conditional.interveningIf()
                        && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forPermanent(target, controllerId))) {
                    continue;
                }
            }
            effectsToResolve.add(effect);
        }
        if (effectsToResolve.isEmpty()) {
            return;
        }

        boolean needsTarget = effectsToResolve.stream()
                .anyMatch(effect -> effect.targetSpec().declaredTarget() != null);
        if (needsTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    card, controllerId, effectsToResolve,
                    "counter placement", target.getId()));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s triggered ability",
                    effectsToResolve,
                    null,
                    target.getId()));
        }
        gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        log.info("Game {} - {} self-counter trigger fires", gameData.id, card.getName());
    }

    /**
     * Fires the Wildwood Scourge-style watcher once for a +1/+1 counter-placement event on a
     * controlled non-Hydra creature other than the watcher.
     */
    private void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target) {
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, target, 1, null, List.of());
    }

    public void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId) {
        firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, target, count, placingPlayerId, List.of());
    }

    public void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target, int count, UUID placingPlayerId,
            List<Permanent> excludedSources) {
        if (count <= 0 || target == null || !gameQueryService.isCreature(gameData, target)
                || predicateEvaluationService.matchesPermanentPredicate(target,
                new PermanentHasSubtypePredicate(CardSubtype.HYDRA),
                FilterContext.of(gameData))) {
            return;
        }

        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (targetControllerId == null) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetControllerId);
        if (battlefield == null) {
            return;
        }
        Set<UUID> excludedSourceIds = excludedSources.stream().map(Permanent::getId)
                .collect(java.util.stream.Collectors.toSet());
        for (Permanent watcher : new ArrayList<>(battlefield)) {
            if (watcher.getId().equals(target.getId()) || excludedSourceIds.contains(watcher.getId())) {
                continue;
            }
            Card card = watcher.getCard();
            List<CardEffect> effects = card.getEffects(
                    EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE);
            if (effects.isEmpty()) {
                continue;
            }

            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    targetControllerId,
                    card.getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    null,
                    watcher.getId()));
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
            log.info("Game {} - {} triggers for a +1/+1 counter on another non-Hydra creature",
                    gameData.id, card.getName());
        }
    }

    public void recordCounterPlacedOnCreature(GameData gameData, Permanent target, UUID placingPlayerId) {
        if (placingPlayerId != null && target != null && gameQueryService.isCreature(gameData, target)) {
            gameData.playersWhoPutCountersOnCreaturesThisTurn.add(placingPlayerId);
        }
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(GameData gameData, Permanent target) {
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, 1);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(GameData gameData, Permanent target,
                                                                       int count) {
        if (target == null) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                gameData, target, controllerId, count);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, UUID controllerId) {
        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, target, controllerId, 1);
    }

    public void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent target, UUID controllerId, int count) {
        if (target != null && controllerId != null) {
            boolean firstPlacementOnThisPermanent =
                    gameData.permanentsThatReceivedPlusOnePlusOneCountersThisTurn.add(target.getId());
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(controllerId);
            firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count);
            if (gameQueryService.isCreature(gameData, target)) {
                firePlusOnePlusOneCountersPutOnControlledCreatureTriggers(gameData, controllerId, count);
            }
            if (firstPlacementOnThisPermanent) {
                fireFirstPlusOnePlusOneCounterPlacementOnAnotherPermanentTriggers(
                        gameData, target, controllerId);
            }
        }
    }

    private void fireFirstPlusOnePlusOneCounterPlacementOnAnotherPermanentTriggers(
            GameData gameData, Permanent target, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            if (source.getId().equals(target.getId())) {
                continue;
            }
            List<CardEffect> effects = source.getCard().getEffects(
                    EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN);
            if (effects.isEmpty()) {
                continue;
            }
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    source.getCard(),
                    controllerId,
                    source.getCard().getName() + "'s triggered ability",
                    new ArrayList<>(effects),
                    null,
                    source.getId()));
            gameLogService.append(gameData, GameLog.cardThen(
                    source.getCard(), "'s triggered ability triggers."));
        }
    }

    private void firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(GameData gameData,
                                                                              UUID controllerId, int count) {
        firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count,
                EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT);
    }

    private void firePlusOnePlusOneCountersPutOnControlledCreatureTriggers(GameData gameData,
                                                                            UUID controllerId, int count) {
        firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(gameData, controllerId, count,
                EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE);
    }

    private void firePlusOnePlusOneCountersPutOnControlledPermanentTriggers(GameData gameData,
                                                                              UUID controllerId, int count,
                                                                              EffectSlot slot) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return;
        }
        for (Permanent source : new ArrayList<>(battlefield)) {
            List<CardEffect> effects = source.getCard().getEffects(slot);
            if (effects.isEmpty()) {
                continue;
            }
            Card card = source.getCard();
            List<CardEffect> effectsToResolve = new ArrayList<>();
            boolean markOnAcceptance = false;
            boolean markImmediately = false;
            for (CardEffect effect : effects) {
                if (effect instanceof OncePerTurnTriggerEffect oncePerTurnTrigger) {
                    if (gameData.oncePerTurnTriggersFiredThisTurn.contains(source.getId())) {
                        continue;
                    }
                    if (oncePerTurnTrigger.markOnAcceptance()) {
                        markOnAcceptance = true;
                    } else {
                        markImmediately = true;
                    }
                    effectsToResolve.add(oncePerTurnTrigger.wrapped());
                } else {
                    effectsToResolve.add(effect);
                }
            }
            if (effectsToResolve.isEmpty()) {
                continue;
            }
            StackEntry triggerEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    card,
                    controllerId,
                    card.getName() + "'s triggered ability",
                    effectsToResolve,
                    null,
                    source.getId()
            );
            triggerEntry.setEventValue(count);
            triggerEntry.setMarkSourceOncePerTurnOnAcceptance(markOnAcceptance);
            gameData.stack.add(triggerEntry);
            if (markImmediately) {
                gameData.oncePerTurnTriggersFiredThisTurn.add(source.getId());
            }
            gameLogService.append(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
            log.info("Game {} - {} +1/+1 counter-on-controlled-permanent trigger fires", gameData.id,
                    card.getName());
        }
    }

    private UUID placingPlayerId(GameData gameData, StackEntry entry, Permanent target) {
        if (entry != null && entry.getControllerId() != null) {
            return entry.getControllerId();
        }
        if (gameData.currentlyResolvingControllerId != null) {
            return gameData.currentlyResolvingControllerId;
        }
        return gameQueryService.findPermanentController(gameData, target.getId());
    }
}
