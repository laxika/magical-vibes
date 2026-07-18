package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TapPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapPermanentsEffect) effect;
        switch (e.scope()) {
            case TARGET -> resolveTarget(gameData, entry, effect);
            case SELF -> resolveSelf(gameData, entry);
            case ENCHANTED -> resolveEnchanted(gameData, entry);
            case TARGET_PLAYERS_PERMANENTS -> resolveTargetPlayersPermanents(gameData, entry, e);
            case ALL_CREATURES -> resolveAllCreatures(gameData, entry, e);
            default -> throw new IllegalStateException("Unsupported tap scope: " + e.scope());
        }
    }

    private void resolveTarget(GameData gameData, StackEntry entry, CardEffect effect) {
        // Multi-target: tap each valid target of this effect's target group — the group's slice
        // of the flat target list for effects bound via target(...).addEffect(...) (e.g. Vibrant
        // Outburst: "3 damage to any target. Tap up to one target creature."), or the whole flat
        // list for unbound effects. An empty group (optional target omitted) taps nothing.
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (!targetIds.isEmpty()) {
            for (UUID targetId : targetIds) {
                Permanent target = gameQueryService.findPermanentById(gameData, targetId);
                if (target == null) {
                    continue;
                }
                tapTarget(gameData, entry, target);
            }
            return;
        }

        // Single-target fallback
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) {
            return;
        }

        tapTarget(gameData, entry, target);
    }

    private void tapTarget(GameData gameData, StackEntry entry, Permanent target) {
        tapUntapSupport.tapPermanent(gameData, target);

        String logEntry = entry.getCard().getName() + " taps " + target.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", target.getCard(), "."));

        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), target.getCard().getName());
    }

    private void resolveSelf(GameData gameData, StackEntry entry) {
        UUID selfId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
        Permanent self = gameQueryService.findPermanentById(gameData, selfId);
        if (self == null) {
            return;
        }

        tapUntapSupport.tapPermanent(gameData, self);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(self.getCard(), " taps itself."));
        log.info("Game {} - {} taps itself", gameData.id, self.getCard().getName());
    }

    private void resolveEnchanted(GameData gameData, StackEntry entry) {
        Permanent auraPerm = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (auraPerm == null) {
            log.info("Game {} - Aura {} no longer on battlefield, skipping tap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        UUID enchantedId = auraPerm.getAttachedTo();
        if (enchantedId == null) {
            log.info("Game {} - {} is not attached to anything, skipping tap enchanted creature",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, enchantedId);
        if (enchantedCreature == null) {
            log.info("Game {} - Enchanted creature no longer on battlefield, skipping tap",
                    gameData.id);
            return;
        }

        tapUntapSupport.tapPermanent(gameData, enchantedCreature);

        String logMsg = entry.getCard().getName() + " taps " + enchantedCreature.getCard().getName() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", enchantedCreature.getCard(), "."));
        log.info("Game {} - {} taps enchanted creature {}", gameData.id, entry.getCard().getName(), enchantedCreature.getCard().getName());
    }

    private void resolveTargetPlayersPermanents(GameData gameData, StackEntry entry, TapPermanentsEffect e) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
        if (battlefield == null) return;

        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        int count = 0;
        for (Permanent p : battlefield) {
            if (!predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) continue;

            if (tapUntapSupport.tapPermanent(gameData, p)) {
                count++;
            }
        }

        String logMsg = entry.getCard().getName() + " taps " + count + " permanent(s).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" taps " + count + " permanent(s).").build());
        log.info("Game {} - {} taps {} permanent(s) of target player", gameData.id, entry.getCard().getName(), count);
    }

    private void resolveAllCreatures(GameData gameData, StackEntry entry, TapPermanentsEffect e) {
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(entry.getControllerId());

        final int[] count = {0};
        gameData.forEachPermanent((playerId, p) -> {
            if (!gameQueryService.isCreature(gameData, p)) return;
            if (e.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(p, e.filter(), filterContext)) return;

            if (tapUntapSupport.tapPermanent(gameData, p)) {
                count[0]++;
            }
        });

        String logMsg = entry.getCard().getName() + " taps " + count[0] + " creature(s).";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(entry.getCard()).text(" taps " + count[0] + " creature(s).").build());
        log.info("Game {} - {} taps {} creature(s) matching filter", gameData.id, entry.getCard().getName(), count[0]);
    }
}
