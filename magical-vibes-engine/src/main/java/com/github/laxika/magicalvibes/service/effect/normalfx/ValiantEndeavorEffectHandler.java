package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ValiantEndeavorEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Resolves Valiant Endeavor's two-roll choice and reuses the ordinary destruction/token effects. */
@Component
@RequiredArgsConstructor
public class ValiantEndeavorEffectHandler implements NormalEffectHandlerBean {

    private final DiceRollService diceRollService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ValiantEndeavorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int firstRoll = entry.getXValue();
        int secondRoll = entry.getEventValue();
        if (firstRoll == 0 || secondRoll == 0) {
            firstRoll = diceRollService.roll(6);
            secondRoll = diceRollService.roll(6);
            entry.setXValue(firstRoll);
            entry.setEventValue(secondRoll);

            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " rolls two d6 for " + entry.getCard().getName() + ": "
                    + firstRoll + " and " + secondRoll + "."));
            triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                    gameData, entry.getControllerId(), 2, Math.max(firstRoll, secondRoll));
            triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                    gameData, entry.getControllerId(), 6, firstRoll, secondRoll);
        }

        int chosenRoll;
        if (firstRoll == secondRoll) {
            chosenRoll = firstRoll;
        } else if (gameData.chosenSpellNumber == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellNumberChoice(
                    gameData, entry.getControllerId(), List.of(firstRoll, secondRoll));
            return;
        } else {
            chosenRoll = gameData.chosenSpellNumber;
            gameData.chosenSpellNumber = null;
            if (chosenRoll != firstRoll && chosenRoll != secondRoll) {
                throw new IllegalStateException("Chosen Valiant Endeavor roll was not rolled");
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        int knightCount = chosenRoll == firstRoll ? secondRoll : firstRoll;
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            return;
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new DestroyAllPermanentsEffect(new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentPowerAtLeastPredicate(chosenRoll)))),
                new CreateTokenEffect(knightCount, "Knight", 2, 2, CardColor.WHITE,
                        List.of(CardSubtype.KNIGHT), Set.of(Keyword.VIGILANCE), Set.of())));
    }
}
