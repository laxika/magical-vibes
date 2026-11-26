package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CantAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link CantAttackThisTurnEffect}: each permanent a named subject finds gets the record as a floating effect
 * for its duration; a set becomes a rule on each of its players, found as it resolves, whose creatures matching the
 * set's filter can't attack whenever attackers are declared, those arriving later included (CR 611.2c; read by
 * {@code AttackLegalityService}); a set but the permanents another subject names is that rule with those permanents,
 * found now, left out.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CantAttackThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CantAttackThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CantAttackThisTurnEffect) effect;
        String period = e.duration() == Duration.UNTIL_END_OF_COMBAT ? "this combat" : "this turn";
        if (e.subject() instanceof PermanentSubject.ControlledBy set) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, e, set.players())) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                        entry.getSourcePermanentId(), entry.getControllerId(), e, playerId, e.duration()));
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(
                        "Creatures controlled by " + playerName + " can't attack " + period + "."));
                log.info("Game {} - creatures controlled by {} can't attack {}", gameData.id, playerName, period);
            }
            return;
        }
        if (e.subject() instanceof PermanentSubject.Except except
                && except.permanents() instanceof PermanentSubject.ControlledBy set) {
            PermanentPredicate rule = subjectResolutionService.lastingRule(gameData, entry, e, except);
            if (rule == null) {
                return;
            }
            CantAttackThisTurnEffect floated = new CantAttackThisTurnEffect(
                    Subjects.controlledBy(set.players(), rule), e.duration());
            for (UUID playerId : subjectResolutionService.players(gameData, entry, e, set.players())) {
                gameData.addFloatingEffect(FloatingContinuousEffect.onPlayer(entry.getCard().getName(),
                        entry.getSourcePermanentId(), entry.getControllerId(), floated, playerId, e.duration()));
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(
                        "Other creatures controlled by " + playerName + " can't attack " + period + "."));
                log.info("Game {} - other creatures controlled by {} can't attack {}", gameData.id,
                        playerName, period);
            }
            return;
        }
        String when = e.duration() == Duration.DURING_AFFECTED_PLAYERS_NEXT_TURN
                ? "during its controller's next turn" : "this turn";
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            gameData.addEffectOnPermanent(entry, e, target.getId(), e.duration());
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " can't attack " + when + "."));
            log.info("Game {} - {} can't attack {}", gameData.id, target.getCard().getName(), when);
        }
    }
}
