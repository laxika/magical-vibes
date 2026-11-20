package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link SkipNextUntapEffect}: each permanent the resolver finds as it resolves is marked; with
 * {@code matchAtUntap} a set is also a rule registered on each of its players (found as it resolves), whose filter the
 * untap step checks against that player's permanents, so later arrivals are held too (CR 611.2c).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SkipNextUntapEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SkipNextUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SkipNextUntapEffect) effect;
        if (e.subject() instanceof PermanentSubject.ControlledBy set) {
            resolveSet(gameData, entry, e, set);
            return;
        }
        UUID stepOwnerId = e.controllerStepOnly() ? entry.getControllerId() : null;
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, e, e.subject())) {
            gameData.skipNextUntapSteps(entry, permanent.getId(), e.untapSteps(), stepOwnerId);
            if (e.subject() instanceof PermanentSubject.Source && e.controllerStepOnly()) {
                // Exert (CR 701.43a): the creature is exerted now, which is what "whenever you exert a creature" watches.
                ActionEvents.exerted(gameData, entry.getControllerId(), permanent.getId());
                if (e.exertsAsItAttacks()) {
                    ActionEvents.exertedAsItAttacks(gameData, entry.getControllerId(), permanent);
                }
            }
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                    " won't untap during its controller's next untap step."));
            log.info("Game {} - {} skip next untap set", gameData.id, permanent.getCard().getName());
        }
    }

    /**
     * A set: each permanent found now is marked, for its controller's next untap step when the set names particular
     * players' permanents ("during that player's next untap step", "your next untap step"; a permanent that changes
     * control before then untaps normally under its new controller), else for whichever untap step of its controller
     * comes next ("their controller's next untap step"). With {@code matchAtUntap} the set is also a rule registered on
     * each of its players, which holds whatever matches as that player's untap step begins.
     */
    private void resolveSet(GameData gameData, StackEntry entry, SkipNextUntapEffect e,
                            PermanentSubject.ControlledBy set) {
        if (e.matchAtUntap()) {
            for (UUID playerId : subjectResolutionService.players(gameData, entry, e, set.players())) {
                gameData.restrictNextUntapStep(entry, playerId, e);
            }
        }
        boolean everyPlayer = set.players() instanceof PlayerSubject.Each each
                && each.relation() == PlayerSubject.Relation.ALL;
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, e, set);
        for (Permanent permanent : permanents) {
            gameData.skipNextUntapSteps(entry, permanent.getId(), e.untapSteps(),
                    everyPlayer ? null : gameData.findControllerOf(permanent));
        }
        String text = switch (set.players()) {
            case PlayerSubject.You ignored -> " prevents " + permanents.size()
                    + " permanent(s) you control from untapping during your next untap step.";
            case PlayerSubject.TargetPlayer ignored ->
                    " prevents matching permanents from untapping during the target player's next untap step.";
            default -> " prevents " + permanents.size()
                    + " permanent(s) from untapping during their controller's next untap step.";
        };
        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(text).build());
        log.info("Game {} - {} skip next untap set on {} permanent(s)", gameData.id, entry.getCard().getName(),
                permanents.size());
    }
}
