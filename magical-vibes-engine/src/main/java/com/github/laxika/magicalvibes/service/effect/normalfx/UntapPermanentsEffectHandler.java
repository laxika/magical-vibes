package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class UntapPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapPermanentsEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry);
            case ALL_TARGETS -> resolveAllTargets(gameData, entry);
            case SELF -> resolveSelf(gameData, entry);
            case SOURCE_PERMANENT -> resolveSourcePermanent(gameData, entry);
            case ENCHANTED -> resolveEnchanted(gameData, entry);
            case CONTROLLED -> resolveControlled(gameData, entry, e);
            case OTHER_CONTROLLED_CREATURES -> resolveOtherControlledCreatures(gameData, entry, e);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case ATTACKED_CREATURES -> resolveAttackedCreatures(gameData, entry);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e);
            case ALL_PERMANENTS -> resolveAllPermanents(gameData, entry, e);
            default -> throw new IllegalStateException("Unsupported untap scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, target);

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " untaps ", target.getCard(), "."));

        log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveAllTargets(GameData gameData, StackEntry entry) {
        List<UUID> targetIds = entry.getTargetIds().isEmpty()
                ? (entry.getTargetId() != null ? List.of(entry.getTargetId()) : List.of())
                : entry.getTargetIds();

        for (UUID targetId : targetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            tapUntapSupport.untapPermanent(gameData, target);

            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " untaps ", target.getCard(), "."));
            log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
        }
    }

    private void resolveSelf(GameData gameData, StackEntry entry) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, self);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps."));

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    private void resolveSourcePermanent(GameData gameData, StackEntry entry) {
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        tapUntapSupport.untapPermanent(gameData, source);

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps."));

        log.info("Game {} - {} untaps", gameData.id, entry.getCard().getName());
    }

    private void resolveEnchanted(GameData gameData, StackEntry entry) {
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping untap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping untap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping untap", gameData.id);
            return;
        }

        tapUntapSupport.untapPermanent(gameData, enchantedCreature);

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " untaps ", enchantedCreature.getCard(), "."));
        log.info("Game {} - {} untaps enchanted creature {}", gameData.id, entry.getCard().getName(), enchantedCreature.getCard().getName());
    }

    private void resolveControlled(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        if (e.chosenCount() > 0) {
            resolveChosenControlled(gameData, entry, e, controllerId, battlefield);
            return;
        }

        int count = 0;
        for (Permanent p : battlefield) {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps " + count + " permanent(s) you control."));
        log.info("Game {} - {} untaps {} controlled permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    /**
     * "Untap up to N permanents you control" (Rewind, Unwind). The controller chooses which tapped
     * permanents to untap at resolution; picking none is legal. Untapping the first N in battlefield
     * order would deny that choice.
     */
    private void resolveChosenControlled(GameData gameData, StackEntry entry, UntapPermanentsEffect e,
                                         UUID controllerId, List<Permanent> battlefield) {
        List<UUID> validIds = new ArrayList<>();
        for (Permanent p : battlefield) {
            if (!p.isTapped()) continue;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(gameData, p, e.filter())) continue;

            validIds.add(p.getId());
        }
        if (validIds.isEmpty()) {
            return;
        }

        int maxCount = Math.min(e.chosenCount(), validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, validIds, maxCount,
                new MultiPermanentChoiceContext.UntapChosenPermanents(entry.getCard().getName()),
                entry.getCard().getName() + " — Choose up to " + maxCount + " permanent"
                        + (maxCount == 1 ? "" : "s") + " to untap.");
    }

    private void resolveOtherControlledCreatures(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID controllerId = entry.getControllerId();
        UUID sourceId = entry.getSourcePermanentId();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (p.getId().equals(sourceId)) continue;
            if (!gameQueryService.isCreature(gameData, p)) continue;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps " + count + " other creature(s) you control."));
        log.info("Game {} - {} untaps {} other creature(s)", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            List<UUID> targets = entry.targetsForEffect(e);
            targetPlayerId = targets.isEmpty() ? null : targets.getFirst();
        }
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;
            if (!p.isTapped()) continue;

            tapUntapSupport.untapPermanent(gameData, p);
            count++;
        }

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps " + count + " permanent(s)."));
        log.info("Game {} - {} untaps {} permanent(s) of target player", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) return;
            if (!p.isTapped()) return;

            if (tapUntapSupport.untapPermanent(gameData, p)) {
                count[0]++;
            }
        });

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps " + count[0] + " creature(s)."));
        log.info("Game {} - {} untaps {} creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    private void resolveAllPermanents(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        if (e.chosenCount() > 0) {
            resolveChosenAllPermanents(gameData, entry, e);
            return;
        }

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) return;
            if (!p.isTapped()) return;

            tapUntapSupport.untapPermanent(gameData, p);
            count[0]++;
        });

        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps " + count[0] + " permanent(s)."));
        log.info("Game {} - {} untaps {} permanent(s)", gameData.id, entry.getCard().getName(), count[0]);
    }

    private void resolveChosenAllPermanents(GameData gameData, StackEntry entry, UntapPermanentsEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard() != null ? entry.getCard().getId() : null)
                .withSourceControllerId(entry.getControllerId());

        List<UUID> validIds = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.isTapped()) return;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(permanent, e.filter(), filterContext)) return;
            validIds.add(permanent.getId());
        });

        if (validIds.isEmpty()) {
            return;
        }

        int maxCount = Math.min(e.chosenCount(), validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, maxCount,
                new MultiPermanentChoiceContext.UntapChosenPermanents(entry.getCard().getName()),
                entry.getCard().getName() + " — Choose up to " + maxCount + " permanent"
                        + (maxCount == 1 ? "" : "s") + " to untap.");
    }

    private void resolveAttackedCreatures(GameData gameData, StackEntry entry) {
        final int[] count = {0};
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!gameQueryService.isCreature(gameData, permanent)) return;
            if (!permanent.isAttackedThisTurn()) return;
            if (!permanent.isTapped()) return;

            tapUntapSupport.untapPermanent(gameData, permanent);
            count[0]++;
        });

        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" untaps " + count[0] + " creature(s) that attacked this turn.")
                .build());
        log.info("Game {} - {} untaps {} attacked creature(s)", gameData.id, entry.getCard().getName(), count[0]);
    }
}
