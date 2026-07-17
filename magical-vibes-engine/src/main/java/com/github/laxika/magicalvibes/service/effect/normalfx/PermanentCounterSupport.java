package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Shared permanent-counter helpers used by every migrated counter effect handler and by
 * {@code MultiPermanentChoiceHandlerService} (async proliferate / counter-placement re-entry).
 *
 * <p>Extracted verbatim from the original {@code PermanentCounterResolutionService} monolith;
 * behavior (counter placement, +1/+1 vs -1/-1 annihilation, saga lore chapters) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermanentCounterSupport {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;

    public void removeCountersAndTransform(GameData gameData, Permanent self, CounterType counterType, String counterName) {
        // Remove all counters of that type
        switch (counterType) {
            case CHARGE -> self.setCounterCount(CounterType.CHARGE, 0);
            case HATCHLING -> self.setCounterCount(CounterType.HATCHLING, 0);
            case LANDMARK -> self.setCounterCount(CounterType.LANDMARK, 0);
            case SLIME -> self.setCounterCount(CounterType.SLIME, 0);
            case STUDY -> self.setCounterCount(CounterType.STUDY, 0);
            case WISH -> self.setCounterCount(CounterType.WISH, 0);
            case PLUS_ONE_PLUS_ONE -> self.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
            case MINUS_ONE_MINUS_ONE -> self.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 0);
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        }

        String removeLog = "All " + counterName + " counters removed from " + self.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(removeLog));
        log.info("Game {} - All {} counters removed from {}", gameData.id, counterName, self.getCard().getName());

        // Transform
        Card originalCard = self.getOriginalCard();
        if (!self.isTransformed()) {
            Card backFace = originalCard.getBackFaceCard();
            if (backFace != null) {
                String frontName = self.getCard().getName();
                self.setCard(backFace);
                self.setTransformed(true);
                String transformLog = frontName + " transforms into " + backFace.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(transformLog));
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            }
        } else {
            String backName = self.getCard().getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            String transformLog = backName + " transforms into " + originalCard.getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(transformLog));
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        }
    }

    public void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters) {
        if (counters <= 0 || gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);

        String counterText = counters == 1 ? "a +1/+1 counter" : counters + " +1/+1 counters";
        String logEntry = target.getCard().getName() + " gets " + counterText + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, target.getCard().getName(), counters);

        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target);
    }

    public void placeCountersOnPermanents(GameData gameData, StackEntry entry, List<UUID> permanentIds, CounterType counterType) {
        List<String> names = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !gameQueryService.cantHaveCounters(gameData, perm)) {
                switch (counterType) {
                    case AIM -> perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + 1);
                    case CHARGE -> perm.setCounterCount(CounterType.CHARGE, perm.getCounterCount(CounterType.CHARGE) + 1);
                    default -> throw new IllegalArgumentException("Unsupported counter type for placement: " + counterType);
                }
                names.add(perm.getCard().getName());
            }
        }

        if (!names.isEmpty()) {
            String counterName = counterType.name().toLowerCase();
            String logEntry = entry.getCard().getName() + " puts an " + counterName + " counter on "
                    + String.join(", ", names) + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            log.info("Game {} - {} places {} counters on {} permanents", gameData.id,
                    entry.getCard().getName(), counterName, names.size());
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
            String logEntry = entry.getCard().getName() + ": no eligible permanent to put counters on.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

    public void placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                         CounterType counterType, int count) {
        if (gameQueryService.cantHaveCounters(gameData, target)) return;

        String counterName = switch (counterType) {
            case CHARGE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case LORE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.LORE, target.getCounterCount(CounterType.LORE) + 1); yield "lore"; }
            case LOYALTY -> { target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) + count); yield "loyalty"; }
            case PLUS_ONE_PLUS_ONE -> {
                if (count <= 0 || gameQueryService.cantHaveCounters(gameData, target)) {
                    yield null;
                }
                target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + count);
                firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target);
                yield "+1/+1";
            }
            case MINUS_ONE_MINUS_ONE -> {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);
                yield "-1/-1";
            }
            case HATCHLING -> { target.setCounterCount(CounterType.HATCHLING, target.getCounterCount(CounterType.HATCHLING) + count); yield "hatchling"; }
            case HOOFPRINT -> { target.setCounterCount(CounterType.HOOFPRINT, target.getCounterCount(CounterType.HOOFPRINT) + count); yield "hoofprint"; }
            case STUDY -> { target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) + count); yield "study"; }
            case WISH -> { target.setCounterCount(CounterType.WISH, target.getCounterCount(CounterType.WISH) + count); yield "wish"; }
            case SLIME -> { target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) + count); yield "slime"; }
            case STORAGE -> { target.setCounterCount(CounterType.STORAGE, target.getCounterCount(CounterType.STORAGE) + count); yield "storage"; }
            case AIM -> { target.setCounterCount(CounterType.AIM, target.getCounterCount(CounterType.AIM) + count); yield "aim"; }
            case EYEBALL -> { target.setCounterCount(CounterType.EYEBALL, target.getCounterCount(CounterType.EYEBALL) + count); yield "eyeball"; }
            case GROWTH -> { target.setCounterCount(CounterType.GROWTH, target.getCounterCount(CounterType.GROWTH) + count); yield "growth"; }
            case PAGE -> { target.setCounterCount(CounterType.PAGE, target.getCounterCount(CounterType.PAGE) + count); yield "page"; }
            case STUN -> { target.setCounterCount(CounterType.STUN, target.getCounterCount(CounterType.STUN) + count); yield "stun"; }
            case TOWER -> { target.setCounterCount(CounterType.TOWER, target.getCounterCount(CounterType.TOWER) + count); yield "tower"; }
            case TIME -> { target.setCounterCount(CounterType.TIME, target.getCounterCount(CounterType.TIME) + count); yield "time"; }
            case VITALITY -> { target.setCounterCount(CounterType.VITALITY, target.getCounterCount(CounterType.VITALITY) + count); yield "vitality"; }
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        };
        if (counterName == null) return;

        Card card = target.getCard();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        String logEntry = entry.getCard().getName() + " puts " + counterText + " on " + card.getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} puts {} {} counter(s) on {}", gameData.id,
                entry.getCard().getName(), count, counterName, card.getName());

        // Lore counters on Sagas trigger chapter abilities (MTG Rule 714.3b)
        if (counterType == CounterType.LORE && card.isSaga()) {
            triggerSagaChapter(gameData, entry, target);
        }

        // Flourishing Defenses etc.: "whenever a -1/-1 counter is put on a creature."
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE) {
            fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, count);
        }
    }

    public String counterTypeName(CounterType counterType) {
        return switch (counterType) {
            case CHARGE -> "charge";
            case LORE -> "lore";
            case LOYALTY -> "loyalty";
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case HATCHLING -> "hatchling";
            case STUDY -> "study";
            case WISH -> "wish";
            case SLIME -> "slime";
            case AIM -> "aim";
            default -> counterType.name().toLowerCase();
        };
    }

    private void triggerSagaChapter(GameData gameData, StackEntry entry, Permanent saga) {
        Card card = saga.getCard();
        int loreCount = saga.getCounterCount(CounterType.LORE);

        EffectSlot chapterSlot = switch (loreCount) {
            case 1 -> EffectSlot.SAGA_CHAPTER_I;
            case 2 -> EffectSlot.SAGA_CHAPTER_II;
            case 3 -> EffectSlot.SAGA_CHAPTER_III;
            default -> null;
        };
        if (chapterSlot == null) return;

        List<CardEffect> chapterEffects = card.getEffects(chapterSlot);
        if (chapterEffects.isEmpty()) return;

        String chapterName = switch (loreCount) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
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

        String logEntry = card.getName() + "'s chapter " + chapterName + " ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} chapter {} triggers", gameData.id, card.getName(), chapterName);
    }

    /**
     * Fires the "whenever a -1/-1 counter is put on a creature" global watcher (Flourishing Defenses)
     * once for each of the {@code count} -1/-1 counters just placed on {@code creature}. Every
     * permanent on any battlefield carrying
     * {@link EffectSlot#ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE} triggers under its own
     * controller. Per the Gatherer ruling the ability triggers once for each individual counter, so a
     * separate trigger is pushed per counter. No-op unless {@code creature} is actually a creature.
     */
    public void fireMinusOneMinusOneCounterPutOnCreatureTriggers(GameData gameData, Permanent creature, int count) {
        if (count <= 0 || creature == null || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent source : new ArrayList<>(battlefield)) {
                List<CardEffect> effects = source.getCard().getEffects(EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE);
                if (effects.isEmpty()) {
                    continue;
                }
                Card card = source.getCard();
                for (int i = 0; i < count; i++) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s triggered ability",
                            new ArrayList<>(effects),
                            null,
                            source.getId()
                    ));
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + "'s triggered ability triggers."));
                }
                log.info("Game {} - {} -1/-1-counter watcher fires {} time(s)", gameData.id, card.getName(), count);
            }
        });
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

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s triggered ability",
                new ArrayList<>(effects),
                null,
                target.getId()
        ));

        String logEntry = card.getName() + "'s triggered ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} +1/+1 counter trigger fires", gameData.id, card.getName());
    }
}
