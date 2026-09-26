package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutPermanentsThatReceivedCountersThisWayEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves the post-proliferate choice of controlled permanents to phase out. */
@Component
@RequiredArgsConstructor
public class PhaseOutPermanentsThatReceivedCountersThisWayEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutPermanentsThatReceivedCountersThisWayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> validIds = controlledRecordedPermanentIds(gameData, entry);
        if (validIds.isEmpty()) {
            return;
        }

        playerInputService.beginMultiPermanentChoice(
                gameData, entry.getControllerId(), validIds, validIds.size(),
                new MultiPermanentChoiceContext.PhaseOutPermanentsThatReceivedCountersThisWay(),
                "Choose any number of permanents you control that received counters this way to phase out.");
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds, StackEntry entry) {
        List<UUID> validIds = controlledRecordedPermanentIds(gameData, entry);
        List<Permanent> toPhaseOut = permanentIds.stream()
                .filter(validIds::contains)
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (toPhaseOut.isEmpty()) {
            return;
        }

        phasingService.phaseOut(gameData, toPhaseOut);
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" phases out " + toPhaseOut.size() + " permanent(s).")
                .build());
    }

    private List<UUID> controlledRecordedPermanentIds(GameData gameData, StackEntry entry) {
        List<UUID> controlledIds = gameData.playerBattlefields
                .getOrDefault(entry.getControllerId(), List.of())
                .stream()
                .map(Permanent::getId)
                .toList();
        return entry.getCounteredPermanentIdsThisResolution().stream()
                .filter(controlledIds::contains)
                .distinct()
                .toList();
    }
}
