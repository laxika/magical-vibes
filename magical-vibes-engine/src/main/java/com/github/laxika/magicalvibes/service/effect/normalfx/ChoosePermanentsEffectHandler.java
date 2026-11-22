package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link ChoosePermanentsEffect}: asks for a single permanent or for several, pauses at its end on the choice
 * and records {@link StepKind#CHOOSE} as it is answered. The log names no face-down permanent's card (CR 708.2).
 */
@Component
@RequiredArgsConstructor
public class ChoosePermanentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SubjectResolutionService subjectResolutionService;
    private final AmountEvaluationService amountEvaluationService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChoosePermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChoosePermanentsEffect) effect;
        List<Permanent> candidates = subjectResolutionService.permanents(gameData, entry, effect, choose.candidates())
                .stream().filter(permanent -> !gameQueryService.cantBeChosen(gameData, permanent)).toList();
        entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
        AmountContext context = AmountContext.forStackEntry(entry, entry.getSourcePermanentSnapshot());
        int maximum = choose.maximum() == null ? candidates.size() : Math.min(candidates.size(),
                amountEvaluationService.evaluate(gameData, choose.maximum(), context));
        if (maximum <= 0) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": there is nothing to choose."));
            return;
        }
        int minimum = Math.min(maximum,
                Math.max(0, amountEvaluationService.evaluate(gameData, choose.minimum(), context)));
        if (minimum == candidates.size()) {
            record(gameData, entry, choose, candidates);
            return;
        }
        List<UUID> ids = candidates.stream().map(Permanent::getId).toList();
        String prompt = entry.getCard().getName() + " — choose " + choose.label() + ".";
        if (minimum == 1 && maximum == 1) {
            var single = new PermanentChoiceContext.RecordChosenPermanents(choose);
            gameData.interaction.setPermanentChoiceContext(single);
            playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), ids, single, prompt);
        } else {
            playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), ids, maximum,
                    new MultiPermanentChoiceContext.ChoosePermanents(choose, minimum), prompt);
        }
    }

    /** Rejects an answer before anything is changed: too few, or two that share a quality that must differ. */
    public void validateChoice(GameData gameData, List<UUID> chosenIds,
                               MultiPermanentChoiceContext.ChoosePermanents context) {
        if (chosenIds.size() < context.minimum()) {
            throw new IllegalStateException("At least " + context.minimum() + " permanents must be selected");
        }
        ChoosePermanentsEffect.Different different = context.effect().different();
        if (different == null) {
            return;
        }
        Set<Integer> values = new HashSet<>();
        for (UUID id : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent == null) {
                continue;
            }
            int value = switch (different) {
                case POWER -> gameQueryService.getEffectivePower(gameData, permanent);
                case MANA_VALUE -> gameQueryService.getPermanentManaValue(permanent);
            };
            if (!values.add(value)) {
                throw new IllegalStateException("The selected permanents must have different " + different.plural());
            }
        }
    }

    /** The answer to the choice. */
    public void completeChoice(GameData gameData, List<UUID> chosenIds, ChoosePermanentsEffect choose) {
        List<Permanent> chosen = new ArrayList<>();
        for (UUID id : chosenIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, id);
            if (permanent != null) {
                chosen.add(permanent);
            }
        }
        record(gameData, gameData.pendingEffectResolutionEntry, choose, chosen);
    }

    private void record(GameData gameData, StackEntry entry, ChoosePermanentsEffect choose, List<Permanent> chosen) {
        String chooser = gameData.playerIdToName.get(entry.getControllerId());
        if (chosen.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(chooser + " chooses nothing."));
            return;
        }
        List<UUID> controllers = new ArrayList<>();
        var log = GameLog.builder().text(chooser + " chooses ");
        for (int i = 0; i < chosen.size(); i++) {
            Permanent permanent = chosen.get(i);
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null) {
                controllers.add(controllerId);
            }
            if (i > 0) {
                log.text(", ");
            }
            if (permanent.isFaceDown()) {
                log.text("a face-down permanent");
            } else {
                log.card(permanent.getCard());
            }
        }
        gameLogService.append(gameData, log.text(".").build());
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(
                chosen.stream().map(Permanent::getId).toList(), controllers));
        if (new Fixed(1).equals(choose.maximum())) {
            Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (source != null) {
                source.setChosenPermanentId(chosen.getFirst().getId());
            }
        }
    }
}
