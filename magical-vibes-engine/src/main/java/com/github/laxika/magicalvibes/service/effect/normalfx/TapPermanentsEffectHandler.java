package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.PlayerSubject;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Taps the permanents a {@link TapPermanentsEffect}'s subject names, found by {@link SubjectResolutionService}. A set
 * subject ("tap all creatures target player controls") is found once and tapped together; with a {@code chosenCount}
 * the controller instead chooses up to that many of the set (Yosei, the Morning Star).
 *
 * <p>A {@link StepKind#TAP} step: it records the permanents it tapped, those already tapped or unable to become tapped
 * left out ("for each creature tapped this way"); the up-to-N choice records them as it is answered.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TapPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TapPermanentsEffect) effect;
        PermanentSubject subject = e.subject();
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, subject);
        entry.recordStep(StepKind.TAP, StepOutcome.NONE);
        List<Permanent> tapped = new ArrayList<>();
        if (!subject.isSet()) {
            for (Permanent permanent : permanents) {
                if (tapNamed(gameData, entry, subject, permanent)) {
                    tapped.add(permanent);
                }
            }
            record(gameData, entry, tapped);
            return;
        }
        if (e.chosenCount() > 0) {
            beginChoice(gameData, entry, e, permanents);
            return;
        }
        // "Tap all permanents you control": you are the player instructed to tap them.
        boolean youTap = subject instanceof PermanentSubject.ControlledBy controlledBy
                && controlledBy.players() instanceof PlayerSubject.You;
        for (Permanent permanent : permanents) {
            boolean newlyTapped = youTap
                    ? tapUntapSupport.tapPermanent(gameData, permanent, entry.getControllerId())
                    : tapUntapSupport.tapPermanent(gameData, permanent);
            if (newlyTapped) {
                tapped.add(permanent);
            }
        }
        record(gameData, entry, tapped);
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " taps " + tapped.size() + " permanent(s)."));
        log.info("Game {} - {} taps {} permanent(s)", gameData.id, entry.getCard().getName(), tapped.size());
    }

    /** Records what the step tapped, with each one's controller, for the steps after it. */
    public static void record(GameData gameData, StackEntry entry, List<Permanent> tapped) {
        if (entry == null) {
            return;
        }
        List<UUID> controllers = new ArrayList<>();
        for (Permanent permanent : tapped) {
            gameData.forEachBattlefield((playerId, battlefield) -> {
                if (battlefield.stream().anyMatch(onField -> onField.getId().equals(permanent.getId()))) {
                    controllers.add(playerId);
                }
            });
        }
        entry.recordStep(StepKind.TAP, StepOutcome.ofObjects(tapped.stream().map(Permanent::getId).toList(),
                controllers));
    }

    private boolean tapNamed(GameData gameData, StackEntry entry, PermanentSubject subject, Permanent permanent) {
        boolean newlyTapped = tapUntapSupport.tapPermanent(gameData, permanent);
        if (subject instanceof PermanentSubject.Source) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " taps itself."));
        } else {
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", permanent.getCard(), "."));
        }
        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), permanent.getCard().getName());
        return newlyTapped;
    }

    /**
     * "Tap up to N target permanents that player controls" (Yosei, the Morning Star). The choice is made at
     * resolution by the ability's controller; picking none is legal.
     */
    private void beginChoice(GameData gameData, StackEntry entry, TapPermanentsEffect e, List<Permanent> permanents) {
        if (permanents.isEmpty()) {
            return;
        }
        int maxCount = Math.min(e.chosenCount(), permanents.size());
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(),
                permanents.stream().map(Permanent::getId).toList(), maxCount,
                new MultiPermanentChoiceContext.TapChosenPermanents(entry.getCard().getName()),
                entry.getCard().getName() + " — Choose up to " + maxCount + " permanent"
                        + (maxCount == 1 ? "" : "s") + " to tap.");
    }
}
