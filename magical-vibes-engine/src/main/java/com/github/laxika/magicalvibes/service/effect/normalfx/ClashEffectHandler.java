package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ClashEffect;
import com.github.laxika.magicalvibes.model.effect.PlaceClashCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a clash, then inserts the library placement of the revealed cards (decided in APNAP
 * order and moved together, CR 701.30c) and any win reward into the current stack entry.
 * Winning with {@code repeatWhileWinning} inserts another clash after those choices.
 */
@Component
@RequiredArgsConstructor
public class ClashEffectHandler implements NormalEffectHandlerBean {

    private final ClashSupport clashSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ClashEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ClashEffect) effect;

        if (!e.beforeClash().isEmpty() && !e.beforeClashResolved()) {
            // The before-clash instructions must finish (including any player input, such as ordering
            // revealed cards onto the library bottom) before the clash reads the library top, so they
            // run through the ordinary resolution loop ahead of the clash itself.
            List<CardEffect> steps = new ArrayList<>(e.beforeClash());
            steps.add(new ClashEffect(e.beforeClash(), e.onWin(), e.repeatWhileWinning(), true));
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, steps);
            return;
        }
        UUID controllerId = entry.getControllerId();
        UUID opponentId;
        if (e.opponent() != null) {
            opponentId = subjectResolutionService.player(gameData, entry, effect, e.opponent());
        } else if (ChoosePlayerEffectHandler.opponentToBeChosenFirst(gameData, entry,
                e.against(Subjects.playersThisWay(StepKind.CHOOSE)))) {
            return;
        } else {
            opponentId = ChoosePlayerEffectHandler.onlyOpponent(gameData, controllerId);
        }
        boolean won = clashSupport.performClash(gameData, controllerId, opponentId);
        gameData.lastClashWonByController.put(controllerId, won);
        List<CardEffect> followUps = new ArrayList<>();
        followUps.add(new PlaceClashCardsEffect(
                PlaceClashCardsEffectHandler.apnapOrder(gameData, controllerId, opponentId)));
        if (won && e.onWin() != null) followUps.add(e.onWin());
        // A repeat restarts the whole sequence, before-clash effects included (Hoarder's Greed).
        if (won && e.repeatWhileWinning()) followUps.add(new ClashEffect(e.beforeClash(), e.onWin(), true));
        // A repeat re-inserts an equal ClashEffect record, so locate this one by the resolving index
        // rather than indexOf (which would find the first, already-resolved copy).
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUps);
    }
}
