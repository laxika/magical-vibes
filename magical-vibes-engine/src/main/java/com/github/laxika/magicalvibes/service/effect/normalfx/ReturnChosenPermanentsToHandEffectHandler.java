package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Asks for the permanents, then returns them as one event and records the step ({@link BounceSupport}). */
@Component
@RequiredArgsConstructor
public class ReturnChosenPermanentsToHandEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final BounceSupport bounceSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnChosenPermanentsToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ReturnChosenPermanentsToHandEffect) effect;
        entry.recordStep(StepKind.RETURN, StepOutcome.NONE);
        List<Permanent> candidates = choosable(gameData,
                subjectResolutionService.permanents(gameData, entry, effect, choose.candidates()));
        AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot());
        int maximum = choose.maximum() == null ? candidates.size() : Math.min(candidates.size(),
                Math.max(0, amountEvaluationService.evaluate(gameData, choose.maximum(), context)));
        int minimum = Math.min(maximum,
                Math.max(0, amountEvaluationService.evaluate(gameData, choose.minimum(), context)));
        String plural = choose.label() + (maximum == 1 ? "" : "s");
        if (maximum == 0) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " has no " + choose.label() + "s to return."));
            return;
        }
        if (minimum == candidates.size() && !choose.alwaysAsked()) {
            bounceSupport.returnChosenTogether(gameData, entry, candidates);
            return;
        }
        String count = minimum == maximum ? "exactly " + maximum
                : choose.maximum() == null ? "any number of" : "up to " + maximum;
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(),
                candidates.stream().map(Permanent::getId).toList(), maximum,
                new MultiPermanentChoiceContext.ReturnChosenPermanentsToHand(choose, minimum),
                "Choose " + count + " " + (choose.maximum() == null ? choose.label() + "s" : plural)
                        + " to return to their owners' hands.");
    }

    /** The answer: those of the chosen that the subject still names return together. */
    public void completeChoice(GameData gameData, StackEntry entry, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.ReturnChosenPermanentsToHand context) {
        List<Permanent> chosen = new ArrayList<>();
        for (Permanent permanent : choosable(gameData, subjectResolutionService.permanents(
                gameData, entry, context.effect(), context.effect().candidates()))) {
            if (chosenIds.contains(permanent.getId())) {
                chosen.add(permanent);
            }
        }
        if (chosen.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " chooses not to return any permanents."));
            return;
        }
        bounceSupport.returnChosenTogether(gameData, entry, chosen);
    }

    /** What can be chosen of {@code permanents}: a permanent that can't be chosen is neither offered nor counted (CR 608.2d). */
    private List<Permanent> choosable(GameData gameData, List<Permanent> permanents) {
        return permanents.stream().filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent)).toList();
    }
}
