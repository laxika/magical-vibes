package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsThenEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves an all-or-nothing sacrifice of several permanents followed by a reflexive effect. */
@Slf4j
@Component
@RequiredArgsConstructor
public class SacrificePermanentsThenEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificePermanentsThenEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SacrificePermanentsThenEffect sacrificeThen = (SacrificePermanentsThenEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Permanent> matching = matchingPermanents(gameData, entry, sacrificeThen);
        if (matching.size() < sacrificeThen.count()) {
            logNoSacrifice(gameData, entry, sacrificeThen);
            return;
        }

        if (matching.size() > sacrificeThen.count()) {
            List<UUID> matchingIds = matching.stream().map(Permanent::getId).toList();
            playerInputService.beginMultiPermanentChoice(
                    gameData,
                    controllerId,
                    matchingIds,
                    sacrificeThen.count(),
                    new MultiPermanentChoiceContext.SacrificePermanentsThen(
                            controllerId, entry.getCard(), sacrificeThen.count(), sacrificeThen.thenEffect()),
                    entry.getCard().getName() + " — Choose " + sacrificeThen.permanentDescription()
                            + " to sacrifice.");
            return;
        }

        sacrificeAndResolve(gameData, entry, matching.stream().map(Permanent::getId).toList(),
                sacrificeThen.count(), sacrificeThen.thenEffect(), entry.getCard(), controllerId);
    }

    public void resolveAfterChoice(GameData gameData, List<UUID> permanentIds,
                                   MultiPermanentChoiceContext.SacrificePermanentsThen context) {
        StackEntry resolvingEntry = gameData.pendingEffectResolutionEntry;
        UUID sourcePermanentId = resolvingEntry == null ? null : resolvingEntry.getSourcePermanentId();
        sacrificeAndResolve(gameData, resolvingEntry, permanentIds, context.requiredCount(), context.thenEffect(),
                context.sourceCard(), context.controllerId(), sourcePermanentId);
    }

    private List<Permanent> matchingPermanents(GameData gameData, StackEntry entry,
                                                SacrificePermanentsThenEffect effect) {
        UUID controllerId = entry.getControllerId();
        FilterContext filterContext = FilterContext.of(gameData)
                .withSourceCardId(entry.getCard().getId())
                .withSourceControllerId(controllerId)
                .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                .withSourcePermanentId(entry.getSourcePermanentId());
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return List.of();
        }
        List<Permanent> matching = new ArrayList<>();
        for (Permanent permanent : battlefield) {
            if (predicateEvaluationService.matchesPermanentPredicate(permanent, effect.filter(), filterContext)) {
                matching.add(permanent);
            }
        }
        return matching;
    }

    private void sacrificeAndResolve(GameData gameData, StackEntry resolvingEntry, List<UUID> permanentIds,
                                      int requiredCount, CardEffect thenEffect, com.github.laxika.magicalvibes.model.Card sourceCard,
                                      UUID controllerId) {
        UUID sourcePermanentId = resolvingEntry == null ? null : resolvingEntry.getSourcePermanentId();
        sacrificeAndResolve(gameData, resolvingEntry, permanentIds, requiredCount, thenEffect, sourceCard,
                controllerId, sourcePermanentId);
    }

    private void sacrificeAndResolve(GameData gameData, StackEntry resolvingEntry, List<UUID> permanentIds,
                                      int requiredCount, CardEffect thenEffect,
                                      com.github.laxika.magicalvibes.model.Card sourceCard, UUID controllerId,
                                      UUID sourcePermanentId) {
        int sacrificed = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null && controllerId.equals(gameQueryService.findPermanentController(gameData, permanentId))) {
                destructionSupport.sacrificeAndLog(gameData, permanent, controllerId);
                sacrificed++;
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (sacrificed != requiredCount) {
            gameLogService.append(gameData,
                    GameLog.text(gameData.playerIdToName.get(controllerId) + " sacrifices fewer permanents than required."));
            return;
        }

        StackEntry triggeredEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s effect",
                List.of(thenEffect),
                null,
                sourcePermanentId);
        gameData.stack.add(triggeredEntry);
    }

    private void logNoSacrifice(GameData gameData, StackEntry entry, SacrificePermanentsThenEffect effect) {
        String playerName = gameData.playerIdToName.get(entry.getControllerId());
        gameLogService.append(gameData,
                GameLog.text(playerName + " cannot sacrifice " + effect.permanentDescription() + "."));
        log.info("Game {} - {} cannot sacrifice {} for {}", gameData.id, playerName,
                effect.permanentDescription(), entry.getCard().getName());
    }
}
