package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PlayerRestrictionEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Puts a {@link PlayerRestrictionEffect} on the players its subject names as it resolves: a floating effect on each of
 * them (CR 611.2a), or one on every player for "players can't …". A subject that names nobody (a target spell's
 * controller that can't be found) restricts nobody.
 */
@Component
@RequiredArgsConstructor
public class PlayerRestrictionEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerRestrictionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var restriction = (PlayerRestrictionEffect) effect;
        String cantText = " can't " + restriction.restriction().description() + " "
                + durationText(gameData, entry, restriction.duration()) + ".";
        if (restriction.players().equals(Subjects.eachPlayer())) {
            restrict(gameData, entry, restriction, null);
            gameLogService.append(gameData, GameLog.text("Players" + cantText));
            return;
        }

        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, restriction.players());
        players.forEach(playerId -> restrict(gameData, entry, restriction, playerId));
        if (players.isEmpty()) {
            return;
        }
        if (restriction.players().equals(Subjects.eachOpponent())) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(entry.getControllerId()) + "'s opponents" + cantText));
            return;
        }
        for (UUID playerId : players) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId) + cantText));
        }
    }

    private static void restrict(GameData gameData, StackEntry entry, PlayerRestrictionEffect restriction,
                                 UUID playerId) {
        gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(FaceDownVisibility.sourceName(gameData, entry),
                entry.getSourcePermanentId(), entry.getControllerId(), restriction, playerId,
                restriction.duration()));
    }

    private static String durationText(GameData gameData, StackEntry entry, Duration duration) {
        String controllerName = gameData.playerIdToName.get(entry.getControllerId());
        if (Duration.UNTIL_END_OF_TURN.equals(duration)) return "this turn";
        if (Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) return "until " + controllerName + "'s next turn";
        if (Duration.UNTIL_END_OF_YOUR_NEXT_TURN.equals(duration)) {
            return "until the end of " + controllerName + "'s next turn";
        }
        if (Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN.equals(duration)) return "during their next turn";
        if (Duration.UNTIL_PLANESWALK.equals(duration)) return "until a player planeswalks";
        return "for the rest of the game";
    }
}
