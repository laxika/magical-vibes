package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final PredicateEvaluationService predicateEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessEffect) effect;
        if (e.subject().declaresPlayerTarget()) {
            if (subjectResolutionService.players(gameData, entry, effect, Subjects.targetPlayer()).isEmpty()) {
                return;
            }
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    e.subject())) {
                applyEffect(gameData, entry, e, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" sets the base power and toughness of " + count + " creature(s) to "
                            + e.power() + "/" + e.toughness() + " until end of turn.").build());
            return;
        }

        if (Subjects.creaturesYourOpponentsControl().equals(e.subject())) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    e.subject())) {
                applyEffect(gameData, entry, e, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" sets the base power and toughness of " + count
                            + " opponent creature(s) to " + e.power() + "/" + e.toughness()
                            + " until end of turn.").build());
            return;
        }

        if (e.subject() instanceof PermanentSubject.Target) {
            for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, e.subject())) {
                if (e.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(target, e.filter(),
                                FilterContext.of(gameData)
                                        .withSourceCardId(entry.getCard().getId())
                                        .withSourceControllerId(entry.getControllerId())
                                        .withSourcePermanentId(entry.getSourcePermanentId())
                                        .withSourcePermanentSnapshot(entry.getSourcePermanentSnapshot())
                                        .withXValue(entry.getXValue()))) {
                    continue;
                }
                applyEffect(gameData, entry, e, target);
                String description = basePowerToughnessDescription(e);
                gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(description).build());
                log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), description);
            }
            return;
        }

        if (e.subject().isSet()) {
            SetBasePowerToughnessEffect individualEffect = new SetBasePowerToughnessEffect(
                    e.power(), e.toughness(), Subjects.target(), e.duration());
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(e.subject(), e.filter()))) {
                applyEffect(gameData, entry, individualEffect, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" sets the base power and toughness of " + count
                            + " other creature(s) to " + e.power() + "/" + e.toughness()
                            + " until end of turn.").build());
            return;
        }

        // source() ("this creature has base P/T X/Y until end of turn", e.g. Marsh Flitter), and any other subject,
        // resolves against the source.
        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, Subjects.source());
        if (target == null) {
            return;
        }

        resolveOn(gameData, entry, e, target);
    }

    /** Sets {@code target}'s base power and toughness as {@code effect} says, for the ability {@code entry}. */
    public void resolveOn(GameData gameData, StackEntry entry, SetBasePowerToughnessEffect effect, Permanent target) {
        applyEffect(gameData, entry, effect, target);
        String description = basePowerToughnessDescription(effect);
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(description).build());
        log.info("Game {} - {}{}", gameData.id, target.getCard().getName(), description);
    }

    private void applyEffect(GameData gameData, StackEntry entry, SetBasePowerToughnessEffect e, Permanent target) {
        // A one-shot base-P/T setter is a floating layer-7b effect on the permanent with its own timestamp
        // (CR 613.4b, 613.7b): of all applicable 7b setters the latest timestamp wins in the layered pass.
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), entry.getSourcePermanentId(), entry.getControllerId(),
                e, target.getId(), null, null, e.duration(), 0));
    }

    private String basePowerToughnessDescription(SetBasePowerToughnessEffect effect) {
        String duration = Duration.UNTIL_END_OF_TURN.equals(effect.duration())
                ? " until end of turn" : "";
        return effect.power() == null
                ? " has base toughness " + effect.toughness() + duration + "."
                : effect.toughness() == null
                ? " has base power " + effect.power() + duration + "."
                : " has base power and toughness " + effect.power() + "/" + effect.toughness()
                + duration + ".";
    }
}
