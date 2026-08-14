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
import com.github.laxika.magicalvibes.model.effect.DoesntUntapWithCounterEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
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

    private static final DoesntUntapWithCounterEffect FUNGUS_UNTAP_LOCK =
            new DoesntUntapWithCounterEffect(CounterType.FUNGUS);
    private static final RemoveCounterFromSourceEffect REMOVE_FUNGUS_COUNTER =
            new RemoveCounterFromSourceEffect(CounterType.FUNGUS, 1);

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
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
        counters = gameQueryService.replaceCounters(gameData, target, CounterType.PLUS_ONE_PLUS_ONE, counters);
        if (counters <= 0) {
            return;
        }
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
        recordCounterPlacedOnCreature(gameData, target, placingPlayerId(gameData, entry, target));

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
                int placed = gameQueryService.replaceCounters(gameData, perm, counterType, 1);
                if (placed <= 0) {
                    continue;
                }
                switch (counterType) {
                    case AIM -> perm.setCounterCount(CounterType.AIM, perm.getCounterCount(CounterType.AIM) + placed);
                    case CHARGE -> perm.setCounterCount(CounterType.CHARGE, perm.getCounterCount(CounterType.CHARGE) + placed);
                    case FLYING, FIRST_STRIKE, LIFELINK -> {
                        perm.setCounterCount(counterType, perm.getCounterCount(counterType) + placed);
                        perm.setCounterTimestamp(counterType, gameData.nextTimestamp());
                    }
                    default -> throw new IllegalArgumentException("Unsupported counter type for placement: " + counterType);
                }
                recordCounterPlacedOnCreature(gameData, perm, placingPlayerId(gameData, entry, perm));
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

    public void placeCounterOnPermanent(GameData gameData, StackEntry entry, Permanent target,
                                         CounterType counterType, int count) {
        if (gameQueryService.cantHaveCounters(gameData, target)) return;

        count = gameQueryService.replaceCounters(gameData, target, counterType, count);

        String counterName = switch (counterType) {
            case CHARGE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.CHARGE, target.getCounterCount(CounterType.CHARGE) + 1); yield "charge"; }
            case LORE -> { for (int i = 0; i < count; i++) target.setCounterCount(CounterType.LORE, target.getCounterCount(CounterType.LORE) + 1); yield "lore"; }
            case LOYALTY -> { target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) + count); yield "loyalty"; }
            case LUCK -> { target.setCounterCount(CounterType.LUCK, target.getCounterCount(CounterType.LUCK) + count); yield "luck"; }
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
            case CARRION -> { target.setCounterCount(CounterType.CARRION, target.getCounterCount(CounterType.CARRION) + count); yield "carrion"; }
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
            case INVITATION -> { target.setCounterCount(CounterType.INVITATION, target.getCounterCount(CounterType.INVITATION) + count); yield "invitation"; }
            case KI -> { target.setCounterCount(CounterType.KI, target.getCounterCount(CounterType.KI) + count); yield "ki"; }
            case LANDMARK -> { target.setCounterCount(CounterType.LANDMARK, target.getCounterCount(CounterType.LANDMARK) + count); yield "landmark"; }
            case STUDY -> { target.setCounterCount(CounterType.STUDY, target.getCounterCount(CounterType.STUDY) + count); yield "study"; }
            case WISH -> { target.setCounterCount(CounterType.WISH, target.getCounterCount(CounterType.WISH) + count); yield "wish"; }
            case SLEIGHT -> { target.setCounterCount(CounterType.SLEIGHT, target.getCounterCount(CounterType.SLEIGHT) + count); yield "sleight"; }
            case SLIME -> { target.setCounterCount(CounterType.SLIME, target.getCounterCount(CounterType.SLIME) + count); yield "slime"; }
            case STORAGE -> { target.setCounterCount(CounterType.STORAGE, target.getCounterCount(CounterType.STORAGE) + count); yield "storage"; }
            case AIM -> { target.setCounterCount(CounterType.AIM, target.getCounterCount(CounterType.AIM) + count); yield "aim"; }
            case ARROW -> { target.setCounterCount(CounterType.ARROW, target.getCounterCount(CounterType.ARROW) + count); yield "arrow"; }
            case BLAZE -> { target.setCounterCount(CounterType.BLAZE, target.getCounterCount(CounterType.BLAZE) + count); yield "blaze"; }
            case BOUNTY -> { target.setCounterCount(CounterType.BOUNTY, target.getCounterCount(CounterType.BOUNTY) + count); yield "bounty"; }
            case BRIBERY -> { target.setCounterCount(CounterType.BRIBERY, target.getCounterCount(CounterType.BRIBERY) + count); yield "bribery"; }
            case BRICK -> { target.setCounterCount(CounterType.BRICK, target.getCounterCount(CounterType.BRICK) + count); yield "brick"; }
            case GEM -> { target.setCounterCount(CounterType.GEM, target.getCounterCount(CounterType.GEM) + count); yield "gem"; }
            case ELIXIR -> { target.setCounterCount(CounterType.ELIXIR, target.getCounterCount(CounterType.ELIXIR) + count); yield "elixir"; }
            case EON -> { target.setCounterCount(CounterType.EON, target.getCounterCount(CounterType.EON) + count); yield "eon"; }
            case EYEBALL -> { target.setCounterCount(CounterType.EYEBALL, target.getCounterCount(CounterType.EYEBALL) + count); yield "eyeball"; }
            case FADE -> { target.setCounterCount(CounterType.FADE, target.getCounterCount(CounterType.FADE) + count); yield "fade"; }
            case GOLD -> { target.setCounterCount(CounterType.GOLD, target.getCounterCount(CounterType.GOLD) + count); yield "gold"; }
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
            case FUNGUS -> {
                if (count <= 0) { yield null; }
                target.setCounterCount(CounterType.FUNGUS, target.getCounterCount(CounterType.FUNGUS) + count);
                grantFungusCounterAbilities(target);
                yield "fungus";
            }
            case FUSE -> { target.setCounterCount(CounterType.FUSE, target.getCounterCount(CounterType.FUSE) + count); yield "fuse"; }
            case VERSE -> { target.setCounterCount(CounterType.VERSE, target.getCounterCount(CounterType.VERSE) + count); yield "verse"; }
            case ICE -> { target.setCounterCount(CounterType.ICE, target.getCounterCount(CounterType.ICE) + count); yield "ice"; }
            case INFECTION -> { target.setCounterCount(CounterType.INFECTION, target.getCounterCount(CounterType.INFECTION) + count); yield "infection"; }
            case INCUBATION -> { target.setCounterCount(CounterType.INCUBATION, target.getCounterCount(CounterType.INCUBATION) + count); yield "incubation"; }
            case MAGNET -> { target.setCounterCount(CounterType.MAGNET, target.getCounterCount(CounterType.MAGNET) + count); yield "magnet"; }
            case MANIFESTATION -> { target.setCounterCount(CounterType.MANIFESTATION, target.getCounterCount(CounterType.MANIFESTATION) + count); yield "manifestation"; }
            case MINE -> { target.setCounterCount(CounterType.MINE, target.getCounterCount(CounterType.MINE) + count); yield "mine"; }
            case MUSIC -> { target.setCounterCount(CounterType.MUSIC, target.getCounterCount(CounterType.MUSIC) + count); yield "music"; }
            case MUSTER -> { target.setCounterCount(CounterType.MUSTER, target.getCounterCount(CounterType.MUSTER) + count); yield "muster"; }
            case WIND -> { target.setCounterCount(CounterType.WIND, target.getCounterCount(CounterType.WIND) + count); yield "wind"; }
            case WAGE -> { target.setCounterCount(CounterType.WAGE, target.getCounterCount(CounterType.WAGE) + count); yield "wage"; }
            case RUST -> { target.setCounterCount(CounterType.RUST, target.getCounterCount(CounterType.RUST) + count); yield "rust"; }
            case SOOT -> { target.setCounterCount(CounterType.SOOT, target.getCounterCount(CounterType.SOOT) + count); yield "soot"; }
            case SOUL -> { target.setCounterCount(CounterType.SOUL, target.getCounterCount(CounterType.SOUL) + count); yield "soul"; }
            case VORTEX -> { target.setCounterCount(CounterType.VORTEX, target.getCounterCount(CounterType.VORTEX) + count); yield "vortex"; }
            case VELOCITY -> { target.setCounterCount(CounterType.VELOCITY, target.getCounterCount(CounterType.VELOCITY) + count); yield "velocity"; }
            case TRAINING -> { target.setCounterCount(CounterType.TRAINING, target.getCounterCount(CounterType.TRAINING) + count); yield "training"; }
            case THEFT -> { target.setCounterCount(CounterType.THEFT, target.getCounterCount(CounterType.THEFT) + count); yield "theft"; }
            case FLYING, FIRST_STRIKE, LIFELINK -> {
                target.setCounterCount(counterType, target.getCounterCount(counterType) + count);
                if (count > 0) {
                    target.setCounterTimestamp(counterType, gameData.nextTimestamp());
                }
                yield counterType.name().toLowerCase();
            }
            default -> throw new IllegalStateException("Unsupported counter type: " + counterType);
        };
        if (counterName == null || count <= 0) return;

        recordCounterPlacedOnCreature(gameData, target, placingPlayerId(gameData, entry, target));

        Card card = target.getCard();
        String counterText = count == 1 ? "a " + counterName + " counter" : count + " " + counterName + " counters";
        Card sourceCard = entry != null ? entry.getCard() : card;
        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard, " puts " + counterText + " on ", card, "."));
        log.info("Game {} - {} puts {} {} counter(s) on {}", gameData.id,
                sourceCard.getName(), count, counterName, card.getName());

        // Lore counters on Sagas trigger chapter abilities (MTG Rule 714.3b)
        if (entry != null && counterType == CounterType.LORE && card.isSaga()) {
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

    /**
     * Mindbender Spores hands the blocked creature two abilities along with its fungus counters:
     * "This creature doesn't untap during your untap step if it has a fungus counter on it" and
     * "At the beginning of your upkeep, remove a fungus counter from this creature". Both are tied
     * to the counter rather than to the source, so they are granted here (idempotently) wherever
     * fungus counters land, and they outlive the creature that placed them.
     */
    private static void grantFungusCounterAbilities(Permanent target) {
        if (!hasGrantedEffect(target, EffectSlot.STATIC, FUNGUS_UNTAP_LOCK)) {
            target.addPersistentTriggeredEffect(EffectSlot.STATIC, FUNGUS_UNTAP_LOCK);
        }
        if (!hasGrantedEffect(target, EffectSlot.UPKEEP_TRIGGERED, REMOVE_FUNGUS_COUNTER)) {
            target.addPersistentTriggeredEffect(EffectSlot.UPKEEP_TRIGGERED, REMOVE_FUNGUS_COUNTER);
        }
    }

    private static boolean hasGrantedEffect(Permanent target, EffectSlot slot, CardEffect effect) {
        return target.getCard().getEffects(slot).contains(effect)
                || target.getPersistentTriggeredEffects(slot).contains(effect);
    }

    public String counterTypeName(CounterType counterType) {
        return switch (counterType) {
            case CHARGE -> "charge";
            case LORE -> "lore";
            case LOYALTY -> "loyalty";
            case PLUS_ONE_PLUS_ONE -> "+1/+1";
            case PLUS_ONE_PLUS_ZERO -> "+1/+0";
            case PLUS_TWO_PLUS_TWO -> "+2/+2";
            case MINUS_ONE_MINUS_ONE -> "-1/-1";
            case MINUS_ONE_MINUS_ZERO -> "-1/-0";
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
            permanent.setCounterCount(counterType,
                    permanent.getCounterCount(counterType) - count);
        }

        String counterName = counterTypeName(counterType);
        String counterText = count == 1
                ? "a " + counterName + " counter"
                : count + " " + counterName + " counters";
        gameLogService.append(gameData, GameLog.cardThen(
                permanent.getCard(), " removes " + counterText + "."));
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
        log.info("Game {} - {} +1/+1 counter trigger fires", gameData.id, card.getName());
    }

    /**
     * Fires the Wildwood Scourge-style watcher once for a +1/+1 counter-placement event on a
     * controlled non-Hydra creature other than the watcher.
     */
    private void firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
            GameData gameData, Permanent target) {
        if (target == null || !gameQueryService.isCreature(gameData, target)
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
        for (Permanent watcher : new ArrayList<>(battlefield)) {
            if (watcher.getId().equals(target.getId())) {
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
