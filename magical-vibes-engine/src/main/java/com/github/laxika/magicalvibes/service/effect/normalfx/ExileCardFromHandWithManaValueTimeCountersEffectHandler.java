package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithManaValueTimeCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Alaundo the Seer's hand exile and time-counter progression. */
@Component
@RequiredArgsConstructor
public class ExileCardFromHandWithManaValueTimeCountersEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;
    private final RemoveTimeCounterFromExiledCardEffectHandler removeTimeCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileCardFromHandWithManaValueTimeCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID chosenCardId = null;
        for (var located : subjectResolutionService.locatedCards(
                gameData, entry, effect, Subjects.cardsThisWay(StepKind.EXILE))) {
            Card chosen = located.card();
            chosenCardId = chosen.getId();
            gameData.exiledCardsWithAlaundoCastAbility.add(chosenCardId);
            if (chosen.getActivatedAbilities(Zone.HAND).stream()
                    .noneMatch(com.github.laxika.magicalvibes.model.ActivatedAbility::isSuspendsSourceFromHand)) {
                gameData.exiledCardsWithNonSuspendTimeCounters.add(chosenCardId);
            }
            int manaValue = chosen.getManaValue();
            if (manaValue > 0) {
                gameData.exiledCardTimeCounters.put(chosenCardId, manaValue);
                gameLogService.append(gameData,
                        GameLog.cardThen(chosen, " gets " + manaValue + " time counters."));
            }
        }
        for (ExiledCardEntry otherEntry : List.copyOf(gameData.exiledCards)) {
            if (!controllerId.equals(otherEntry.ownerId())
                    || otherEntry.card().getId().equals(chosenCardId)) {
                continue;
            }
            Integer counters = gameData.exiledCardTimeCounters.get(otherEntry.card().getId());
            if (counters != null && counters > 0) {
                removeTimeCounterHandler.removeTimeCounter(gameData, otherEntry.card().getId());
            }
        }
    }
}
