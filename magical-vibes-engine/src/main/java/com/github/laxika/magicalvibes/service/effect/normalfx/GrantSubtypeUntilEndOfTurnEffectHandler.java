package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GrantSubtypeUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantSubtypeUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantSubtypeUntilEndOfTurnEffect) effect;
        if (e.scope() == GrantScope.TARGET_PLAYERS_CREATURES) {
            if (subjectResolutionService.players(gameData, entry, effect, Subjects.targetPlayer()).isEmpty()) {
                return;
            }
            grantToSet(gameData, entry, e, Subjects.narrowed(e.scope().subject(), e.filter()));
            return;
        }

        if (e.scope() == GrantScope.OWN_CREATURES) {
            // This one-shot "creatures you control" includes the source.
            grantToSet(gameData, entry, e, Subjects.narrowed(GrantScope.ALL_OWN_CREATURES.subject(), e.filter()));
            return;
        }

        if (e.scope() == GrantScope.SELF) {
            Permanent self = subjectResolutionService.permanent(gameData, entry, effect, e.scope().subject());
            if (self == null) {
                return;
            }
            applyEffect(gameData, entry, e, self);
            gameLogService.append(gameData, GameLog.builder().card(self.getCard())
                    .text(" becomes a " + e.subtype().getDisplayName()
                            + " in addition to its other types" + durationText(e) + ".").build());
            return;
        }

        // A target group of this effect that was left empty names nothing, not another effect's target.
        if (entry.targetsForEffect(e).isEmpty() && entry.getTargetingCard() != null
                && entry.getTargetingCard().hasEffectTargetIndex(e)) {
            return;
        }
        for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect,
                GrantScope.TARGET.subject())) {
            applyEffect(gameData, entry, e, target);
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" becomes a " + e.subtype().getDisplayName()
                            + " in addition to its other types" + durationText(e) + ".").build());
        }
    }

    private void grantToSet(GameData gameData, StackEntry entry, GrantSubtypeUntilEndOfTurnEffect e,
                            PermanentSubject subject) {
        int count = 0;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, subject)) {
            applyEffect(gameData, entry, e, permanent);
            count++;
        }
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                .text(" grants " + e.subtype().getDisplayName() + " to " + count
                        + " creature(s)" + durationText(e) + ".").build());
    }

    private void applyEffect(GameData gameData, StackEntry entry,
                             GrantSubtypeUntilEndOfTurnEffect effect, Permanent target) {
        gameData.addFloatingEffect(new FloatingContinuousEffect(
                UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(),
                entry.getControllerId(), new GrantSubtypeEffect(effect.subtype(), GrantScope.TARGET),
                target.getId(), null, null, effect.duration(), 0));
    }

    private String durationText(GrantSubtypeUntilEndOfTurnEffect effect) {
        return Duration.UNTIL_END_OF_TURN.equals(effect.duration()) ? " until end of turn" : "";
    }
}
