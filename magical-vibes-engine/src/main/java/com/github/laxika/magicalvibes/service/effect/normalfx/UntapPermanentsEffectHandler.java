package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Untaps the permanents an {@link UntapPermanentsEffect}'s subject names, found by {@link SubjectResolutionService}. A
 * set subject ("untap all creatures you control") is found once and its tapped permanents untapped together; with a
 * {@code chosenCount} the controller instead chooses up to that many of its tapped permanents (Rewind, Unwind).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UntapPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameLogService gameLogService;
    private final TapUntapSupport tapUntapSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return UntapPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (UntapPermanentsEffect) effect;
        PermanentSubject subject = e.subject();
        List<Permanent> permanents = subjectResolutionService.permanents(gameData, entry, effect, subject);
        if (!subject.isSet()) {
            for (Permanent permanent : permanents) {
                untapNamed(gameData, entry, subject, permanent);
            }
            return;
        }
        List<Permanent> tapped = permanents.stream().filter(Permanent::isTapped).toList();
        if (e.chosenCount() > 0) {
            beginChoice(gameData, entry, e, tapped);
            return;
        }
        for (Permanent permanent : tapped) {
            tapUntapSupport.untapPermanent(gameData, permanent);
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                " untaps " + tapped.size() + " permanent(s)."));
        log.info("Game {} - {} untaps {} permanent(s)", gameData.id, entry.getCard().getName(), tapped.size());
    }

    private void untapNamed(GameData gameData, StackEntry entry, PermanentSubject subject, Permanent permanent) {
        tapUntapSupport.untapPermanent(gameData, permanent);
        if (subject instanceof PermanentSubject.Source) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), " untaps."));
        } else {
            gameLogService.append(gameData,
                    GameLog.cardTextCard(entry.getCard(), " untaps ", permanent.getCard(), "."));
        }
        log.info("Game {} - {} untaps {}", gameData.id, entry.getCard().getName(), permanent.getCard().getName());
    }

    /**
     * "Untap up to N permanents you control" (Rewind, Unwind). The controller chooses which tapped permanents to untap
     * at resolution; picking none is legal. Untapping the first N in battlefield order would deny that choice.
     */
    private void beginChoice(GameData gameData, StackEntry entry, UntapPermanentsEffect e, List<Permanent> tapped) {
        if (tapped.isEmpty()) {
            return;
        }
        List<UUID> validIds = tapped.stream().map(Permanent::getId).toList();
        int maxCount = Math.min(e.chosenCount(), validIds.size());
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), validIds, maxCount,
                new MultiPermanentChoiceContext.UntapChosenPermanents(entry.getCard().getName()),
                entry.getCard().getName() + " — Choose up to " + maxCount + " permanent"
                        + (maxCount == 1 ? "" : "s") + " to untap.");
    }
}
