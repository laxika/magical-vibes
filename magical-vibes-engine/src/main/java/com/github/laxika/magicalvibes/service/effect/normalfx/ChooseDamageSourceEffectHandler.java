package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseDamageSourceEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link ChooseDamageSourceEffect}: the controller chooses one of the sources {@link DamageSourceChoiceSupport}
 * offers (CR 609.7a), unasked when there is one (CR 608.2d); the step pauses at its end on the choice and records
 * {@link StepKind#CHOOSE} with the chosen object's id and its controller, if it has one. With none to choose, nothing
 * is chosen. The log names no face-down permanent's card (CR 708.2).
 */
@Component
@RequiredArgsConstructor
public class ChooseDamageSourceEffectHandler implements NormalEffectHandlerBean {

    private final DamageSourceChoiceSupport damageSourceChoiceSupport;
    private final SubjectResolutionService subjectResolutionService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseDamageSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChooseDamageSourceEffect) effect;
        entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
        List<UUID> candidates = damageSourceChoiceSupport.candidates(gameData, null, entry);
        if (choose.controlledBy() != null) {
            Set<UUID> controllers = new HashSet<>(
                    subjectResolutionService.players(gameData, entry, effect, choose.controlledBy()));
            candidates = candidates.stream()
                    .filter(id -> controllers.contains(damageSourceChoiceSupport.controllerOf(gameData, id, entry)))
                    .toList();
        }
        candidates = candidates.stream()
                .filter(id -> {
                    Permanent permanent = gameQueryService.findPermanentById(gameData, id);
                    return permanent == null || !gameQueryService.cantBeChosen(gameData, permanent);
                })
                .toList();
        if (candidates.isEmpty()) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": there is no source to choose."));
            return;
        }
        if (candidates.size() == 1) {
            record(gameData, entry, candidates.getFirst());
            return;
        }
        var context = new PermanentChoiceContext.RecordChosenDamageSource(choose);
        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPermanentChoice(gameData, entry.getControllerId(), candidates, context,
                FaceDownVisibility.sourceName(gameData, entry) + " — choose a source"
                        + (choose.controlledBy() != null ? " you control." : "."));
    }

    /** The answer to the choice. */
    public void completeChoice(GameData gameData, UUID sourceId) {
        record(gameData, gameData.pendingEffectResolutionEntry, sourceId);
    }

    private void record(GameData gameData, StackEntry entry, UUID sourceId) {
        UUID controllerId = damageSourceChoiceSupport.controllerOf(gameData, sourceId, entry);
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
        Card card = permanent == null ? damageSourceChoiceSupport.sourceCard(gameData, sourceId) : null;
        String name = permanent != null ? FaceDownVisibility.publicName(gameData, permanent)
                : card != null ? FaceDownVisibility.publicName(gameData, card) : "a source";
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(entry.getControllerId()) + " chooses " + name + " as the source."));
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofObjects(List.of(sourceId),
                controllerId == null ? List.of() : List.of(controllerId)));
    }
}
