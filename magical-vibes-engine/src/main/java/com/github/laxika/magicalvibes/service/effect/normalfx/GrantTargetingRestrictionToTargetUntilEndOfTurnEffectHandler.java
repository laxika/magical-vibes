package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantTargetingRestrictionToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantTargetingRestrictionToTargetUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantTargetingRestrictionToTargetUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var grant = (GrantTargetingRestrictionToTargetUntilEndOfTurnEffect) effect;
        if (grant.subject() instanceof PermanentSubject.ControlledBy set) {
            restrictSet(gameData, entry, grant, set);
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }
        CardEffect restriction = grant.restriction() instanceof TargetingRestrictionEffect targeting
                && targeting.opponentOnly()
                ? targeting.withOpponentsOf(entry.getControllerId())
                : grant.restriction();
        for (UUID targetId : targetIds) {
            if (gameQueryService.findPermanentById(gameData, targetId) == null) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    entry.getCard().getName(),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantEffectEffect(restriction, Subjects.target()),
                    targetId,
                    null,
                    null,
                    Duration.UNTIL_END_OF_TURN,
                    0));
        }
    }

    /** The rule of a set, judged whenever it is checked (CR 611.2c). */
    private void restrictSet(GameData gameData, StackEntry entry, GrantTargetingRestrictionToTargetUntilEndOfTurnEffect grant,
                             PermanentSubject.ControlledBy set) {
        PermanentPredicate rule = subjectResolutionService.lastingRule(gameData, entry, grant, set);
        if (rule == null) {
            return;
        }
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(),
                entry.getCard().getName(),
                null,
                entry.getControllerId(),
                new GrantNonAbilityEffectEffect(grant.restriction(), Subjects.target()),
                null,
                null,
                rule,
                Duration.UNTIL_END_OF_TURN,
                0));
        gameLogService.append(gameData, GameLog.builder()
                .card(entry.getCard())
                .text(" makes creatures you control unable to be targeted by opponents' spells or abilities until end of turn.")
                .build());
    }
}
