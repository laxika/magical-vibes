package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutPhylacteryCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEachOwnCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEachOtherCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveChargeCountersFromTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersFromTargetAndBoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutXMinusOneMinusOneCountersOnEachCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.UnattachEquipmentFromTargetPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermanentCounterResolutionService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @HandlesEffect(PutXMinusOneMinusOneCountersOnEachCreatureEffect.class)
    private void resolvePutXMinusOneMinusOneCountersOnEachCreature(GameData gameData, StackEntry entry) {
        int xValue = entry.getXValue();
        final int[] count = {0};

        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (gameQueryService.cantHaveCounters(gameData, p)) return;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) return;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + xValue);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " puts " + xValue + " -1/-1 counter(s) on " + count[0] + " creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts {} -1/-1 counter(s) on {} creature(s)", gameData.id, entry.getCard().getName(), xValue, count[0]);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachOtherCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachOtherCreature(GameData gameData, StackEntry entry) {
        UUID sourceId = entry.getSourcePermanentId();
        final int[] count = {0};

        gameData.forEachPermanent((playerId, p) -> {
            if (p.getId().equals(sourceId)) return;
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (gameQueryService.cantHaveCounters(gameData, p)) return;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) return;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count[0] + " other creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} other creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControlsEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachCreatureTargetPlayerControls(GameData gameData, StackEntry entry) {
        UUID targetPlayerId = entry.getTargetPermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) continue;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
            count++;
        }

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count + " creature(s) target player controls.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} creature(s) target player controls", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEachAttackingCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEachAttackingCreature(GameData gameData, StackEntry entry) {
        final int[] count = {0};

        gameData.forEachPermanent((playerId, p) -> {
            if (!p.isAttacking()) return;
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (gameQueryService.cantHaveCounters(gameData, p)) return;
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, p)) return;

            p.setMinusOneMinusOneCounters(p.getMinusOneMinusOneCounters() + 1);
            count[0]++;
        });

        String logEntry = entry.getCard().getName() + " puts a -1/-1 counter on " + count[0] + " attacking creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts -1/-1 counter on {} attacking creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    @HandlesEffect(PutCountersOnSourceEffect.class)
    private void resolvePutCountersOnSource(GameData gameData, StackEntry entry, PutCountersOnSourceEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, source)) {
            return;
        }

        String counterLabel = String.format("%+d/%+d", effect.powerModifier(), effect.toughnessModifier());
        if (effect.powerModifier() > 0) {
            source.setPlusOnePlusOneCounters(source.getPlusOnePlusOneCounters() + effect.amount());
        } else {
            if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, source)) return;
            source.setMinusOneMinusOneCounters(source.getMinusOneMinusOneCounters() + effect.amount());
        }
        String logEntry = source.getCard().getName() + " gets " + effect.amount() + " " + counterLabel + " counter(s).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} {} counter(s)", gameData.id, source.getCard().getName(), effect.amount(), counterLabel);
    }

    @HandlesEffect(PutChargeCounterOnSelfEffect.class)
    private void resolvePutChargeCounterOnSelf(GameData gameData, StackEntry entry) {
        // Use sourcePermanentId (not targetPermanentId) because "self" always refers to the source
        // permanent — targetPermanentId may point to a different target (e.g. a graveyard card).
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        self.setChargeCounters(self.getChargeCounters() + 1);

        String logEntry = self.getCard().getName() + " gets a charge counter (" + self.getChargeCounters() + " total).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a charge counter ({} total)", gameData.id, self.getCard().getName(), self.getChargeCounters());
    }

    @HandlesEffect(PutCounterOnSelfEffect.class)
    private void resolvePutCounterOnSelf(GameData gameData, StackEntry entry, PutCounterOnSelfEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        String counterName = switch (effect.counterType()) {
            case CHARGE -> { self.setChargeCounters(self.getChargeCounters() + 1); yield "charge"; }
            case HATCHLING -> { self.setHatchlingCounters(self.getHatchlingCounters() + 1); yield "hatchling"; }
            case SLIME -> { self.setSlimeCounters(self.getSlimeCounters() + 1); yield "slime"; }
            case STUDY -> { self.setStudyCounters(self.getStudyCounters() + 1); yield "study"; }
            case WISH -> { self.setWishCounters(self.getWishCounters() + 1); yield "wish"; }
            case PLUS_ONE_PLUS_ONE -> { self.setPlusOnePlusOneCounters(self.getPlusOnePlusOneCounters() + 1); yield "+1/+1"; }
            case MINUS_ONE_MINUS_ONE -> {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, self)) { yield null; }
                self.setMinusOneMinusOneCounters(self.getMinusOneMinusOneCounters() + 1);
                yield "-1/-1";
            }
            default -> throw new IllegalStateException("Unsupported counter type for PutCounterOnSelfEffect: " + effect.counterType());
        };
        if (counterName == null) return;

        String logEntry = self.getCard().getName() + " gets a " + counterName + " counter.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a {} counter", gameData.id, self.getCard().getName(), counterName);
    }

    @HandlesEffect(PutCounterOnSelfThenTransformIfThresholdEffect.class)
    private void resolvePutCounterOnSelfThenTransformIfThreshold(GameData gameData, StackEntry entry,
                                                                  PutCounterOnSelfThenTransformIfThresholdEffect effect) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetPermanentId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, self)) {
            return;
        }

        // Put the counter
        String counterName = switch (effect.counterType()) {
            case CHARGE -> { self.setChargeCounters(self.getChargeCounters() + 1); yield "charge"; }
            case HATCHLING -> { self.setHatchlingCounters(self.getHatchlingCounters() + 1); yield "hatchling"; }
            case SLIME -> { self.setSlimeCounters(self.getSlimeCounters() + 1); yield "slime"; }
            case STUDY -> { self.setStudyCounters(self.getStudyCounters() + 1); yield "study"; }
            case WISH -> { self.setWishCounters(self.getWishCounters() + 1); yield "wish"; }
            case PLUS_ONE_PLUS_ONE -> { self.setPlusOnePlusOneCounters(self.getPlusOnePlusOneCounters() + 1); yield "+1/+1"; }
            case MINUS_ONE_MINUS_ONE -> {
                if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, self)) { yield null; }
                self.setMinusOneMinusOneCounters(self.getMinusOneMinusOneCounters() + 1);
                yield "-1/-1";
            }
            default -> throw new IllegalStateException("Unsupported counter type: " + effect.counterType());
        };
        if (counterName == null) return;

        String logEntry = self.getCard().getName() + " gets a " + counterName + " counter.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a {} counter", gameData.id, self.getCard().getName(), counterName);

        // Check threshold and transform if met
        int currentCount = switch (effect.counterType()) {
            case CHARGE -> self.getChargeCounters();
            case HATCHLING -> self.getHatchlingCounters();
            case SLIME -> self.getSlimeCounters();
            case STUDY -> self.getStudyCounters();
            case WISH -> self.getWishCounters();
            case PLUS_ONE_PLUS_ONE -> self.getPlusOnePlusOneCounters();
            case MINUS_ONE_MINUS_ONE -> self.getMinusOneMinusOneCounters();
            default -> 0;
        };

        if (currentCount >= effect.threshold()) {
            // Remove all counters of that type
            switch (effect.counterType()) {
                case CHARGE -> self.setChargeCounters(0);
                case HATCHLING -> self.setHatchlingCounters(0);
                case SLIME -> self.setSlimeCounters(0);
                case STUDY -> self.setStudyCounters(0);
                case WISH -> self.setWishCounters(0);
                case PLUS_ONE_PLUS_ONE -> self.setPlusOnePlusOneCounters(0);
                case MINUS_ONE_MINUS_ONE -> self.setMinusOneMinusOneCounters(0);
                default -> throw new IllegalStateException("Unsupported counter type: " + effect.counterType());
            }

            String removeLog = "All " + counterName + " counters removed from " + self.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, removeLog);
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
                    gameBroadcastService.logAndBroadcast(gameData, transformLog);
                    log.info("Game {} - {} transforms into {}", gameData.id, frontName, backFace.getName());
                }
            } else {
                String backName = self.getCard().getName();
                self.setCard(originalCard);
                self.setTransformed(false);
                String transformLog = backName + " transforms into " + originalCard.getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, transformLog);
                log.info("Game {} - {} transforms into {}", gameData.id, backName, originalCard.getName());
            }
        }
    }

    @HandlesEffect(PutChargeCounterOnTargetPermanentEffect.class)
    private void resolvePutChargeCounterOnTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        target.setChargeCounters(target.getChargeCounters() + 1);

        String logEntry = target.getCard().getName() + " gets a charge counter (" + target.getChargeCounters() + " total).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a charge counter ({} total)", gameData.id, target.getCard().getName(), target.getChargeCounters());
    }

    @HandlesEffect(PutPhylacteryCounterOnTargetPermanentEffect.class)
    private void resolvePutPhylacteryCounterOnTargetPermanent(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        target.setPhylacteryCounters(target.getPhylacteryCounters() + 1);

        String logEntry = target.getCard().getName() + " gets a phylactery counter (" + target.getPhylacteryCounters() + " total).";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets a phylactery counter ({} total)", gameData.id, target.getCard().getName(), target.getPhylacteryCounters());
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnTargetCreature(GameData gameData, StackEntry entry, PutMinusOneMinusOneCounterOnTargetCreatureEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
            return;
        }

        int count = effect.count();
        target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() + count);

        String counterText = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        String logEntry = target.getCard().getName() + " gets " + counterText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} -1/-1 counter(s)", gameData.id, target.getCard().getName(), count);

        if (effect.regenerateIfSurvives()) {
            int effectiveToughness = gameQueryService.getEffectiveToughness(gameData, target);
            if (effectiveToughness >= 1) {
                target.setRegenerationShield(target.getRegenerationShield() + 1);

                String regenLog = target.getCard().getName() + " gains a regeneration shield.";
                gameBroadcastService.logAndBroadcast(gameData, regenLog);
                log.info("Game {} - {} gains a regeneration shield (toughness {})", gameData.id, target.getCard().getName(), effectiveToughness);
            } else {
                log.info("Game {} - {} has toughness {}, no regeneration shield", gameData.id, target.getCard().getName(), effectiveToughness);
            }
        }
    }

    @HandlesEffect(RemoveChargeCountersFromTargetPermanentEffect.class)
    private void resolveRemoveChargeCountersFromTargetPermanent(GameData gameData, StackEntry entry, RemoveChargeCountersFromTargetPermanentEffect effect) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        int toRemove = Math.min(effect.maxCount(), target.getChargeCounters());
        if (toRemove > 0) {
            target.setChargeCounters(target.getChargeCounters() - toRemove);
            String logEntry = toRemove + " charge counter(s) removed from " + target.getCard().getName() + " (" + target.getChargeCounters() + " remaining).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} charge counter(s) removed from {} ({} remaining)", gameData.id, toRemove, target.getCard().getName(), target.getChargeCounters());
        }
    }

    @HandlesEffect(RemoveCountersFromTargetAndBoostSelfEffect.class)
    private void resolveRemoveCountersFromTargetAndBoostSelf(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            return;
        }

        int maxToRemove = entry.getXValue();
        int totalRemoved = 0;

        // Remove counters from all counter types, up to X total
        // Order: +1/+1, charge, loyalty, -1/-1, awakening
        int remaining = maxToRemove;

        if (remaining > 0 && target.getPlusOnePlusOneCounters() > 0) {
            int remove = Math.min(remaining, target.getPlusOnePlusOneCounters());
            target.setPlusOnePlusOneCounters(target.getPlusOnePlusOneCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getChargeCounters() > 0) {
            int remove = Math.min(remaining, target.getChargeCounters());
            target.setChargeCounters(target.getChargeCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getLoyaltyCounters() > 0) {
            int remove = Math.min(remaining, target.getLoyaltyCounters());
            target.setLoyaltyCounters(target.getLoyaltyCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getPhylacteryCounters() > 0) {
            int remove = Math.min(remaining, target.getPhylacteryCounters());
            target.setPhylacteryCounters(target.getPhylacteryCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getSlimeCounters() > 0) {
            int remove = Math.min(remaining, target.getSlimeCounters());
            target.setSlimeCounters(target.getSlimeCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getHatchlingCounters() > 0) {
            int remove = Math.min(remaining, target.getHatchlingCounters());
            target.setHatchlingCounters(target.getHatchlingCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getStudyCounters() > 0) {
            int remove = Math.min(remaining, target.getStudyCounters());
            target.setStudyCounters(target.getStudyCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getMinusOneMinusOneCounters() > 0) {
            int remove = Math.min(remaining, target.getMinusOneMinusOneCounters());
            target.setMinusOneMinusOneCounters(target.getMinusOneMinusOneCounters() - remove);
            totalRemoved += remove;
            remaining -= remove;
        }

        if (remaining > 0 && target.getAwakeningCounters() > 0) {
            int remove = Math.min(remaining, target.getAwakeningCounters());
            target.setAwakeningCounters(target.getAwakeningCounters() - remove);
            totalRemoved += remove;
        }

        if (totalRemoved > 0) {
            String logEntry = totalRemoved + " counter(s) removed from " + target.getCard().getName() + ".";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} counter(s) removed from {}", gameData.id, totalRemoved, target.getCard().getName());
        }

        // Boost source creature +1/+0 per counter removed
        if (totalRemoved > 0) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                source.setPowerModifier(source.getPowerModifier() + totalRemoved);
                String boostLog = source.getCard().getName() + " gets +" + totalRemoved + "/+0 until end of turn.";
                gameBroadcastService.logAndBroadcast(gameData, boostLog);
                log.info("Game {} - {} gets +{}/+0", gameData.id, source.getCard().getName(), totalRemoved);
            }
        }
    }

    @HandlesEffect(PutMinusOneMinusOneCounterOnEnchantedCreatureEffect.class)
    private void resolvePutMinusOneMinusOneCounterOnEnchantedCreature(GameData gameData, StackEntry entry,
                                                                      PutMinusOneMinusOneCounterOnEnchantedCreatureEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        if (gameQueryService.cantHaveCounters(gameData, creature)) {
            return;
        }

        if (gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, creature)) {
            return;
        }

        int count = effect.count();
        creature.setMinusOneMinusOneCounters(creature.getMinusOneMinusOneCounters() + count);

        String counterText = count == 1 ? "a -1/-1 counter" : count + " -1/-1 counters";
        String logEntry = creature.getCard().getName() + " gets " + counterText + " from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} -1/-1 counter(s) from {}", gameData.id, creature.getCard().getName(), count, entry.getCard().getName());
    }

    @HandlesEffect(PutPlusOnePlusOneCounterOnEnchantedCreatureEffect.class)
    private void resolvePutPlusOnePlusOneCounterOnEnchantedCreature(GameData gameData, StackEntry entry,
                                                                    PutPlusOnePlusOneCounterOnEnchantedCreatureEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || !aura.isAttached()) return;

        Permanent creature = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (creature == null) return;

        if (gameQueryService.cantHaveCounters(gameData, creature)) {
            return;
        }

        int count = effect.count();
        creature.setPlusOnePlusOneCounters(creature.getPlusOnePlusOneCounters() + count);

        String counterText = count == 1 ? "a +1/+1 counter" : count + " +1/+1 counters";
        String logEntry = creature.getCard().getName() + " gets " + counterText + " from " + entry.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} +1/+1 counter(s) from {}", gameData.id, creature.getCard().getName(), count, entry.getCard().getName());
    }

    @HandlesEffect(PutPlusOnePlusOneCounterOnTargetCreatureEffect.class)
    private void resolvePutPlusOnePlusOneCounterOnTargetCreature(GameData gameData, StackEntry entry,
                                                                 PutPlusOnePlusOneCounterOnTargetCreatureEffect effect) {
        // Multi-target: apply counters to each valid target
        if (entry.getTargetPermanentIds() != null && !entry.getTargetPermanentIds().isEmpty()) {
            for (UUID targetId : entry.getTargetPermanentIds()) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue; // Partially resolves — skip removed targets
                }
                if (gameQueryService.cantHaveCounters(gameData, target)) {
                    continue;
                }
                applyPlusOnePlusOneCounters(gameData, entry, target, effect.count());
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetPermanentId());
        if (target == null) {
            log.info("Game {} - Target creature no longer on battlefield, effect fizzles", gameData.id);
            return;
        }

        if (gameQueryService.cantHaveCounters(gameData, target)) {
            return;
        }

        applyPlusOnePlusOneCounters(gameData, entry, target, effect.count());
    }

    private void applyPlusOnePlusOneCounters(GameData gameData, StackEntry entry, Permanent target, int counters) {
        target.setPlusOnePlusOneCounters(target.getPlusOnePlusOneCounters() + counters);

        String counterText = counters == 1 ? "a +1/+1 counter" : counters + " +1/+1 counters";
        String logEntry = target.getCard().getName() + " gets " + counterText + ".";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} gets {} +1/+1 counter(s)", gameData.id, target.getCard().getName(), counters);
    }

    @HandlesEffect(ProliferateEffect.class)
    private void resolveProliferate(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();

        // Collect all permanents with counters (any player's battlefield)
        List<UUID> eligiblePermanentIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, p) -> {
            if (p.getPlusOnePlusOneCounters() > 0
                    || p.getMinusOneMinusOneCounters() > 0
                    || p.getLoyaltyCounters() > 0
                    || p.getSlimeCounters() > 0
                    || p.getHatchlingCounters() > 0) {
                eligiblePermanentIds.add(p.getId());
            }
        });

        if (eligiblePermanentIds.isEmpty()) {
            String logEntry = "Proliferate: no permanents with counters to choose.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - Proliferate: no eligible permanents", gameData.id);
            return;
        }

        // Count total proliferate effects in this stack entry (e.g. "proliferate, then proliferate again")
        // so the handler knows how many rounds of choices remain after this one.
        long totalProliferates = entry.getEffectsToResolve().stream()
                .filter(e -> e instanceof ProliferateEffect)
                .count();
        gameData.pendingProliferateCount = (int) totalProliferates;
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, eligiblePermanentIds,
                eligiblePermanentIds.size(), "Proliferate: Choose permanents to add counters to.");
    }

    @HandlesEffect(PutPlusOnePlusOneCounterOnEachOwnCreatureEffect.class)
    private void resolvePutPlusOnePlusOneCounterOnEachOwnCreature(GameData gameData, StackEntry entry) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;

            p.setPlusOnePlusOneCounters(p.getPlusOnePlusOneCounters() + 1);
            count++;
        }

        String logEntry = entry.getCard().getName() + " puts a +1/+1 counter on " + count + " creature(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts +1/+1 counter on {} own creature(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(PutPlusOnePlusOneCounterOnEachControlledPermanentEffect.class)
    private void resolvePutPlusOnePlusOneCounterOnEachControlledPermanent(GameData gameData, StackEntry entry,
                                                                          PutPlusOnePlusOneCounterOnEachControlledPermanentEffect effect) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
        if (battlefield == null) return;

        FilterContext ctx = FilterContext.of(gameData).withSourceCardId(entry.getCard().getId());
        int count = 0;
        for (Permanent p : battlefield) {
            if (!gameQueryService.matchesPermanentPredicate(p, effect.predicate(), ctx)) continue;
            if (gameQueryService.cantHaveCounters(gameData, p)) continue;

            p.setPlusOnePlusOneCounters(p.getPlusOnePlusOneCounters() + 1);
            count++;
        }

        String logEntry = entry.getCard().getName() + " puts a +1/+1 counter on " + count + " permanent(s) you control.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} puts +1/+1 counter on {} controlled permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    @HandlesEffect(UnattachEquipmentFromTargetPermanentsEffect.class)
    private void resolveUnattachEquipmentFromTargetPermanents(GameData gameData, StackEntry entry) {
        if (entry.getTargetPermanentIds() == null || entry.getTargetPermanentIds().isEmpty()) {
            return;
        }

        // Track creatures that need to be sacrificed due to SacrificeOnUnattachEffect
        Set<UUID> sacrificeTargetIds = new LinkedHashSet<>();

        for (UUID targetId : entry.getTargetPermanentIds()) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            gameData.forEachPermanent((playerId, p) -> {
                if (targetId.equals(p.getAttachedTo())
                        && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                    p.setAttachedTo(null);
                    String unattachLog = entry.getCard().getName() + " unattaches " + p.getCard().getName()
                            + " from " + target.getCard().getName() + ".";
                    gameBroadcastService.logAndBroadcast(gameData, unattachLog);
                    log.info("Game {} - {} unattaches {} from {}", gameData.id, entry.getCard().getName(),
                            p.getCard().getName(), target.getCard().getName());

                    boolean hasSacrificeOnUnattach = p.getCard().getEffects(EffectSlot.STATIC).stream()
                            .anyMatch(e -> e instanceof SacrificeOnUnattachEffect);
                    if (hasSacrificeOnUnattach) {
                        sacrificeTargetIds.add(targetId);
                    }
                }
            });
        }

        // Sacrifice creatures that were unattached from equipment with SacrificeOnUnattachEffect
        for (UUID creatureId : sacrificeTargetIds) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureId);
            if (creature != null) {
                String sacrificeLog = creature.getCard().getName() + " is sacrificed (equipment with sacrifice-on-unattach became unattached).";
                gameBroadcastService.logAndBroadcast(gameData, sacrificeLog);
                log.info("Game {} - {} sacrificed due to equipment unattach", gameData.id, creature.getCard().getName());
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                permanentRemovalService.removeOrphanedAuras(gameData);
            }
        }
    }
}
