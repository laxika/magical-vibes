package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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

import java.util.List;

/**
 * Taps the permanents a {@link TapPermanentsEffect}'s subject names, found by {@link SubjectResolutionService}. A set
 * subject ("tap all creatures target player controls") is found once and tapped together; with a {@code chosenCount}
 * the controller instead chooses up to that many of the set (Yosei, the Morning Star).
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
        if (!subject.isSet()) {
            for (Permanent permanent : permanents) {
                tapNamed(gameData, entry, subject, permanent);
            }
            return;
        }
        if (e.chosenCount() > 0) {
            beginChoice(gameData, entry, e, permanents);
            return;
        }
        // "Tap all permanents you control": you are the player instructed to tap them.
        boolean youTap = subject instanceof PermanentSubject.ControlledBy controlledBy
                && controlledBy.players() instanceof PlayerSubject.You;
        int count = 0;
        for (Permanent permanent : permanents) {
            boolean tapped = youTap
                    ? tapUntapSupport.tapPermanent(gameData, permanent, entry.getControllerId())
                    : tapUntapSupport.tapPermanent(gameData, permanent);
            if (tapped) {
                count++;
            }
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " taps " + count + " permanent(s)."));
        log.info("Game {} - {} taps {} permanent(s)", gameData.id, entry.getCard().getName(), count);
    }

    private void tapNamed(GameData gameData, StackEntry entry, PermanentSubject subject, Permanent permanent) {
        tapUntapSupport.tapPermanent(gameData, permanent);
        if (subject instanceof PermanentSubject.Source) {
            gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " taps itself."));
        } else {
            gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " taps ", permanent.getCard(), "."));
        }
        log.info("Game {} - {} taps {}", gameData.id, entry.getCard().getName(), permanent.getCard().getName());
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
