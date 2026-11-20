package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantColorUntilEndOfTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantColorUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantColorUntilEndOfTurnEffect) effect;
        PermanentSubject subject = e.subject();
        if (subject instanceof PermanentSubject.AllTargets) {
            // "Any number of target creatures become [color]": every target of this effect's group; an empty group
            // names nothing.
            List<Permanent> targets = entry.targetsForEffect(e).isEmpty() ? List.of()
                    : subjectResolutionService.permanents(gameData, entry, effect, Subjects.target());
            int count = 0;
            for (Permanent target : targets) {
                applyEffect(gameData, entry, e, target);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " target creature(s) " + colorName(e)
                            + " until end of turn.").build());
            return;
        }

        if (subject.declaresPlayerTarget()) {
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
                    .text(" makes " + count + " creature(s) " + colorName(e) + " until end of turn.").build());
            return;
        }

        if (subject.isSet()) {
            int count = 0;
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
                applyEffect(gameData, entry, e, permanent);
                count++;
            }
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" makes " + count + " creature(s) " + colorName(e) + " until end of turn.").build());
            return;
        }

        if (subject instanceof PermanentSubject.Source || subject instanceof PermanentSubject.Triggering) {
            // "It becomes [color]": the permanent whose event triggered the ability (Shadow Puppeteers).
            Permanent target = subjectResolutionService.permanent(gameData, entry, effect, subject);
            if (target == null) {
                return;
            }
            applyEffect(gameData, entry, e, target);
            String suffix = e.additive()
                    ? " in addition to its other colors until end of turn."
                    : " until end of turn.";
            gameLogService.append(gameData, GameLog.builder().card(target.getCard())
                    .text(" becomes " + colorName(e) + suffix).build());
            log.info("Game {} - {} becomes {}{}", gameData.id, target.getCard().getName(),
                    colorName(e), suffix.substring(0, suffix.length() - 1));
            return;
        }

        Permanent target = subjectResolutionService.permanent(gameData, entry, effect, Subjects.target());
        if (target == null) {
            if (e.canTargetSpell()) {
                StackEntry targetSpell = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
                if (targetSpell != null) {
                    gameData.addCardEffect(entry,
                            new com.github.laxika.magicalvibes.model.effect.SpellColorUntilEndOfTurnEffect(e.color()),
                            entry.getTargetId(), com.github.laxika.magicalvibes.model.Zone.STACK,
                            com.github.laxika.magicalvibes.model.effect.Duration.UNTIL_END_OF_TURN);
                    String colorName = e.color().name().charAt(0)
                            + e.color().name().substring(1).toLowerCase();
                    gameLogService.append(gameData, GameLog.builder()
                            .card(targetSpell.getCard())
                            .text(" becomes " + colorName + " until end of turn.")
                            .build());
                    log.info("Game {} - spell {} becomes {} until end of turn",
                            gameData.id, targetSpell.getCard().getName(), colorName);
                }
            }
            return;
        }

        applyEffect(gameData, entry, e, target);

        String colorName = colorName(e);
        String suffix = e.additive() ? " in addition to its other colors until end of turn." : " until end of turn.";
        gameLogService.append(gameData, GameLog.builder().card(target.getCard()).text(" becomes " + colorName + suffix).build());

        log.info("Game {} - {} becomes {} until end of turn", gameData.id, target.getCard().getName(), colorName);
    }

    private String colorName(GrantColorUntilEndOfTurnEffect e) {
        return e.color().name().charAt(0) + e.color().name().substring(1).toLowerCase();
    }

    private void applyEffect(GameData gameData, StackEntry entry, GrantColorUntilEndOfTurnEffect e, Permanent target) {
        // CR 613 layer engine: "becomes [color] until end of turn" is a floating layer-5
        // color effect with its own timestamp — it beats earlier setters (an already
        // attached Nim Deathmantle) and loses to later ones, then wears off at cleanup.
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(), e,
                target.getId(), null, null, Duration.UNTIL_END_OF_TURN, 0));
    }
}
