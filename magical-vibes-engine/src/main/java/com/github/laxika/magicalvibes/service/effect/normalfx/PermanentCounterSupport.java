package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(frontCard, " transforms into ", backFace, "."));
                log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
            }
        } else {
            Card backCard = self.getCard();
            String backName = backCard.getName();
            self.setCard(originalCard);
            self.setTransformed(false);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(backCard, " transforms into ", originalCard, "."));
            log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
        }
    }

    public void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters) {
        if (counters <= 0 || gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);

        String counterText = counters == 1 ? "a +1/+1 counter" : counters + " +1/+1 counters";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " gets " + counterText + "."));
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, target.getCard().getName(), counters);

        firePlusOnePlusOneCountersPutOnSelfTriggers(gameData, target);
    }

    public void placeCountersOnPermanents(GameData gameData, StackEntry entry, List<UUID> permanentIds, CounterType counterType) {
        List<Card> affectedCards = new ArrayList<>();
        for (UUID permId : permanentIds) {
            Permanent perm = gameQueryService.findPermanentById(gameData, permId);
            if (perm != null && !gameQueryService.cantHaveCounters(gameData, perm)) {
                switch (counterType) {
                    case AIM -> perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + 1);
                    case CHARGE -> perm.setCounterCount(CounterType.CHARGE, perm.getCounterCount(CounterType.CHARGE) + 1);
                    default -> throw new IllegalArgumentException("Unsupported counter type for placement: " + counterType);
                }
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
            gameBroadcastService.logAndBroadcast(gameData, builder.build());
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), ": no eligible permanent to put counters on."));
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
                count = gameQueryService.reduceMinusOneMinusOneCounters(gameData, target, count);
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + count);
                yield "-1/-1";
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
            case CARRION -> { target.setCounterCount(CounterType.CARRION, target.getCounterCount(CounterType.CARRION) + count); yield "carrion"; }
            case DOOM -> { target.setCounterCount(CounterType.DOOM, target.getCounterCount(CounterType.DOOM) + count); yield "doom"; }
            case CORPSE -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.CORPSE, target.getCounterCount(CounterType.CORPSE) + count);
                yield "corpse";
            }
            case HATCHLING -> { target.setCounterCount(CounterType.HATCHLING, target.getCounterCount(CounterType.HATCHLING) + count); yield "hatchling"; }
            case HOOFPRINT -> { target.setCounterCount(CounterType.HOOFPRINT, target.getCounterCount(CounterType.HOOFPRINT) + count); yield "hoofprint"; }
            case INVITATION -> { target.setCounterCount(CounterType.INVITATION, target.getCounterCount(CounterType.INVITATION) + count); yield "invitation"; }
            case STUDY -> { target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) + count); yield "study"; }
            case WISH -> { target.setCounterCount(CounterType.WISH, target.getCounterCount(CounterType.WISH) + count); yield "wish"; }
            case SLIME -> { target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) + count); yield "slime"; }
            case STORAGE -> { target.setCounterCount(CounterType.STORAGE, target.getCounterCount(CounterType.STORAGE) + count); yield "storage"; }
            case AIM -> { target.setCounterCount(CounterType.AIM, target.getCounterCount(CounterType.AIM) + count); yield "aim"; }
            case BRIBERY -> { target.setCounterCount(CounterType.BRIBERY, target.getCounterCount(CounterType.BRIBERY) + count); yield "bribery"; }
            case BRICK -> { target.setCounterCount(CounterType.BRICK, target.getCounterCount(CounterType.BRICK) + count); yield "brick"; }
            case EYEBALL -> { target.setCounterCount(CounterType.EYEBALL, target.getCounterCount(CounterType.EYEBALL) + count); yield "eyeball"; }
            case GROWTH -> { target.setCounterCount(CounterType.GROWTH, target.getCounterCount(CounterType.GROWTH) + count); yield "growth"; }
            case PAGE -> { target.setCounterCount(CounterType.PAGE, target.getCounterCount(CounterType.PAGE) + count); yield "page"; }
            case STUN -> { target.setCounterCount(CounterType.STUN, target.getCounterCount(CounterType.STUN) + count); yield "stun"; }
            case TOWER -> { target.setCounterCount(CounterType.TOWER, target.getCounterCount(CounterType.TOWER) + count); yield "tower"; }
            case TIME -> { target.setCounterCount(CounterType.TIME, target.getCounterCount(CounterType.TIME) + count); yield "time"; }
            case AGE -> { target.setCounterCount(CounterType.AGE, target.getCounterCount(CounterType.AGE) + count); yield "age"; }
            case VITALITY -> { target.setCounterCount(CounterType.VITALITY, target.getCounterCount(CounterType.VITALITY) + count); yield "vitality"; }
            case HEALING -> { target.setCounterCount(CounterType.HEALING, target.getCounterCount(CounterType.HEALING) + count); yield "healing"; }
            case FEATHER -> { target.setCounterCount(CounterType.FEATHER, target.getCounterCount(CounterType.FEATHER) + count); yield "feather"; }
            case PARALYZATION -> { target.setCounterCount(CounterType.PARALYZATION, target.getCounterCount(CounterType.PARALYZATION) + count); yield "paralyzation"; }
            case ICE -> { target.setCounterCount(CounterType.ICE, target.getCounterCount(CounterType.ICE) + count); yield "ice"; }
            case MUSIC -> { target.setCounterCount(CounterType.MUSIC, target.getCounterCount(CounterType.MUSIC) + count); yield "music"; }
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        };
        if (counterName == null) return;

        Card card = target.getCard();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " puts " + counterText + " on ", card, "."));
        log.info("Game {} - {} puts {} {} counter(s) on {}", gameData.id,
                entry.getCard().getName(), count, counterName, card.getName());

        // Lore counters on Sagas trigger chapter abilities (MTG Rule 714.3b)
        if (counterType == CounterType.LORE && card.isSaga()) {
            triggerSagaChapter(gameData, entry, target);
        }

        // Flourishing Defenses etc.: "whenever a -1/-1 counter is put on a creature." The placing player
        // is the resolving spell/ability's controller — read it from the entry rather than
        // currentlyResolvingControllerId, which is null when resolution was resumed asynchronously after a
        // target choice (e.g. Hapatra's combat-damage "put a -1/-1 counter on target creature").
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE) {
            UUID placingPlayerId = entry != null ? entry.getControllerId() : gameData.currentlyResolvingControllerId;
            fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, count, placingPlayerId);
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s chapter " + chapterName + " ability triggers."));
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
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
                    log.info("Game {} - {} once-per-creature -1/-1-counter watcher fires", gameData.id, card.getName());
                }
            }
        });

        // Self-scoped "Whenever you put one or more -1/-1 counters on this creature" (Defiant Greatmaw):
        // fires once per event (not per counter), only when the controller is the placing player.
        fireSelfMinusOneMinusOneCountersPutTriggers(gameData, creature, placingPlayerId);
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

        boolean needsTarget = effects.stream().anyMatch(e -> e.targetSpec().category() != TargetCategory.NONE);
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
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(card, "'s triggered ability triggers — choose a target."));
        } else {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName() + "'s triggered ability",
                    new ArrayList<>(effects), null, creature.getId()));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
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

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(card, "'s triggered ability triggers."));
        log.info("Game {} - {} +1/+1 counter trigger fires", gameData.id, card.getName());
    }
}
