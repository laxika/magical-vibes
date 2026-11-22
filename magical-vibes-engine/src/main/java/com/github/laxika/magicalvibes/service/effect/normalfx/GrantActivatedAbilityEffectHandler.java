package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GrantActivatedAbilityEffect) effect;
        PermanentSubject subject = e.subject();
        int count = 0;
        if (subject instanceof PermanentSubject.Source) {
            Permanent source = subjectResolutionService.permanent(gameData, entry, effect, subject);
            if (source != null) {
                grantTo(gameData, entry, source, e);
                count++;
            }
        } else if (subject instanceof PermanentSubject.Target) {
            if ((Duration.WHILE_SOURCE_REMAINS.equals(e.duration())
                    || Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(e.duration()))
                    && (entry.getSourcePermanentId() == null
                    || gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) == null)) {
                return;
            }
            // "Target creature gains '[ability]' until end of turn" (e.g. Banishing Knack).
            for (Permanent target : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
                grantTo(gameData, entry, target, e);
                count++;
            }
        } else if (subject instanceof PermanentSubject.Attached) {
            // The Genju cycle: "enchanted land", no creature check.
            Permanent enchanted = subjectResolutionService.permanent(gameData, entry, effect, subject);
            if (enchanted != null) {
                grantTo(gameData, entry, enchanted, e);
                count++;
            }
        } else {
            // Any other subject has always meant the creatures you control, the source included.
            PermanentSubject set = subject.isSet() ? subject : Subjects.creaturesYouControl();
            for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect,
                    Subjects.narrowed(set, e.filter()))) {
                grantTo(gameData, entry, permanent, e);
                count++;
            }
        }

        Duration duration = e.duration();
        String durationText = Duration.UNTIL_YOUR_NEXT_TURN.equals(duration) ? "until your next turn"
                : Duration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE.equals(duration)
                ? "until the source card is cast from exile"
                : Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration) || Duration.WHILE_SOURCE_REMAINS.equals(duration)
                ? "for as long as the source remains on the battlefield"
                : Duration.PERMANENT.equals(duration) || Duration.STATIC.equals(duration) ? "indefinitely"
                : "until end of turn";
        String recipientText = subject instanceof PermanentSubject.Source ? "permanent(s)" :
                Subjects.landsYouControl().equals(subject) ? "land(s)" :
                Subjects.permanentsYouControl().equals(subject) ? "permanent(s)" : "creature(s)";
        
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" grants \"" + e.ability().getDescription() + "\" to " + count + " " + recipientText + " " + durationText + ".").build());
        log.info("Game {} - {} grants activated ability to {} {} {}",
                gameData.id, entry.getCard().getName(), count, recipientText, durationText);
    }

    private static void grantTo(GameData gameData, StackEntry entry, Permanent permanent,
                                GrantActivatedAbilityEffect grant) {
        Duration duration = grant.duration();
        if (Duration.WHILE_SOURCE_ON_BATTLEFIELD.equals(duration)
                || Duration.WHILE_SOURCE_REMAINS.equals(duration)
                || Duration.PERMANENT.equals(duration)
                || Duration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE.equals(duration)
                // "Until your next turn" ends on the turn of the effect's controller, even if the
                // permanent has changed control since (Hydro-Man, Fluid Felon).
                || Duration.UNTIL_YOUR_NEXT_TURN.equals(duration)) {
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(),
                    FaceDownVisibility.sourceName(gameData, entry),
                    entry.getSourcePermanentId(),
                    entry.getControllerId(),
                    new GrantActivatedAbilityEffect(
                            grantedAbility(grant.ability(), entry).withGrantSource(entry.getSourcePermanentId()),
                            Subjects.target(),
                            grant.filter(),
                            duration,
                            grant.expirationCardId() != null
                                    ? grant.expirationCardId() : entry.getCard().getId()
                    ),
                    permanent.getId(),
                    null,
                    null,
                    duration,
                    0
            ));
            return;
        }
        // Until end of turn, the one other duration a resolving grant states.
        gameData.addEffectOnPermanent(entry, new GrantActivatedAbilityEffect(grantedAbility(grant.ability(), entry),
                Subjects.target(), grant.filter(), Duration.UNTIL_END_OF_TURN), permanent.getId(),
                Duration.UNTIL_END_OF_TURN);
    }

    private static ActivatedAbility grantedAbility(ActivatedAbility ability, StackEntry entry) {
        ActivatedAbility granted = ability.withGrantingPlayer(entry.getControllerId());
        // CR 602.5c: a whole-game "activate only once" cap belongs to the ability as acquired from this
        // object, not to other identically worded abilities (a later casting grants a fresh one).
        return granted.getMaxActivationsPerGame() != null ? granted.withAcquisitionId(UUID.randomUUID()) : granted;
    }
}
