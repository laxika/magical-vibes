package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PlayerCounterType;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentSubject;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;

/**
 * Resolves {@link AddAnotherCounterOfChosenTypeToTargetEffect}: asks which kind of counter on the target permanent or
 * player gets another one, or, over a set, asks for each permanent in turn (or skip).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AddAnotherCounterOfChosenTypeToTargetEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final SubjectResolutionService subjectResolutionService;
    @Autowired private InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddAnotherCounterOfChosenTypeToTargetEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        PermanentSubject subject = ((AddAnotherCounterOfChosenTypeToTargetEffect) effect).subject();
        if (!subject.declaresTarget()) {
            askEach(gameData, entry, effect, subject);
            return;
        }
        List<UUID> targetIds = entry.targetsForEffect(effect);
        UUID targetId = !targetIds.isEmpty() ? targetIds.getFirst() : entry.getTargetId();
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null) {
            List<CounterType> counterTypes = new ArrayList<>();
            for (CounterType type : CounterType.values()) {
                if (type != CounterType.ANY && type != CounterType.SILVER
                        && target.getCounterCount(type) > 0) {
                    counterTypes.add(type);
                }
            }
            if (counterTypes.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), " finds no counters on the target."));
                return;
            }
            playerInputService.beginAddAnotherCounterTypeChoice(gameData, entry.getControllerId(), targetId,
                    entry.getCard().getName(), counterTypes);
            return;
        }

        if (gameData.playerIds.contains(targetId)) {
            // Any kind of counter the player has (CR 122.1).
            List<PlayerCounterType> playerCounterTypes = Arrays.stream(PlayerCounterType.values())
                    .filter(kind -> gameData.playerCounterCount(targetId, kind) > 0)
                    .toList();
            if (playerCounterTypes.isEmpty()) return;
            ChoiceContext.AddAnotherCounterTypeChoice context = new ChoiceContext.AddAnotherCounterTypeChoice(
                    targetId, entry.getControllerId(), entry.getCard().getName(), List.of(),
                    playerCounterTypes, false, null);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    entry.getControllerId(), null, null, context, context.options(),
                    entry.getCard().getName() + " — Choose a counter to add another of."));
        }
    }

    /** "For each [permanent], choose a counter on it. You may put an additional counter of that kind on it." */
    private void askEach(GameData gameData, StackEntry entry, CardEffect effect, PermanentSubject subject) {
        List<UUID> remaining = new ArrayList<>();
        for (Permanent permanent : subjectResolutionService.permanents(gameData, entry, effect, subject)) {
            if (!PermanentCounterSupport.counterKindsOn(permanent).isEmpty()) {
                remaining.add(permanent.getId());
            }
        }
        if (remaining.isEmpty()) {
            return;
        }
        UUID first = remaining.removeFirst();
        Permanent permanent = gameQueryService.findPermanentById(gameData, first);
        playerInputService.beginAddAnotherCounterTypeOnEachNonSagaPermanentChoice(gameData, entry.getControllerId(),
                first, entry.getCard().getName(), remaining, PermanentCounterSupport.counterKindsOn(permanent));
    }
}
