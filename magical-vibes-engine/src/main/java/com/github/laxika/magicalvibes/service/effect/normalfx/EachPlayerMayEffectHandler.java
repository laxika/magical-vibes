package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Asks each player {@link EachPlayerMayEffect#deciders()} names, in APNAP order, changing nothing while they decide,
 * then records the players who said yes as the entry's accepted players and puts {@code forAcceptors} next in the
 * parked resolution, once, for all of them (CR 101.4).
 */
@Component
@RequiredArgsConstructor
public class EachPlayerMayEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerMayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var question = (EachPlayerMayEffect) effect;
        List<UUID> deciders = new ArrayList<>(subjectResolutionService.players(gameData, entry, effect,
                question.deciders()));
        deciders.removeIf(id -> !gameData.playerIds.contains(id));
        entry.setAcceptedPlayerIds(List.of());
        if (!deciders.isEmpty()) {
            askNext(gameData, entry.getCard(), entry.getSourcePermanentId(),
                    FaceDownVisibility.sourceName(gameData, entry) + " — " + question.prompt(),
                    question.deciding(deciders, List.of()));
        }
    }

    /**
     * Applies one player's answer: the next player is asked, or, once everyone has decided, the players who said yes
     * are recorded and X is put next in the parked resolution.
     */
    public void completeChoice(GameData gameData, PendingMayAbility ability, EachPlayerMayEffect question,
                               boolean accepted) {
        UUID playerId = ability.controllerId();
        List<UUID> acceptedIds = new ArrayList<>(question.acceptedPlayerIds());
        if (accepted) {
            acceptedIds.add(playerId);
        }
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                + (accepted ? " says yes." : " says no.")));

        List<UUID> remaining = new ArrayList<>(question.remainingPlayerIds());
        remaining.remove(playerId);
        remaining.removeIf(id -> !gameData.playerIds.contains(id));
        if (!remaining.isEmpty()) {
            askNext(gameData, ability.sourceCard(), ability.sourcePermanentId(), ability.description(),
                    question.deciding(remaining, acceptedIds));
            return;
        }

        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }
        acceptedIds.removeIf(id -> !gameData.playerIds.contains(id));
        entry.setAcceptedPlayerIds(acceptedIds);
        if (!acceptedIds.isEmpty()) {
            entry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex, List.of(question.forAcceptors()));
        }
    }

    private void askNext(GameData gameData, Card sourceCard, UUID sourcePermanentId, String prompt,
                         EachPlayerMayEffect question) {
        UUID playerId = question.remainingPlayerIds().getFirst();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(sourceCard, playerId, List.of(question),
                prompt, null, null, sourcePermanentId));
    }
}
