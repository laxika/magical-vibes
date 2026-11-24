package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RollTwoDiceChooseOneEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Rolls two dice once and asks the controller which result to use. The dice are rolled before the choice and kept on
 * the entry (first as X, second as the event value) while the prompt is open, so resolving again after the answer
 * never rolls twice; the answer then writes the chosen result as X and the other as the event value.
 */
@Component
@RequiredArgsConstructor
public class RollTwoDiceChooseOneEffectHandler implements NormalEffectHandlerBean {

    private final DiceRollService diceRollService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollTwoDiceChooseOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int sides = ((RollTwoDiceChooseOneEffect) effect).sides();
        int firstRoll = entry.getXValue();
        int secondRoll = entry.getEventValue();
        if (firstRoll == 0 || secondRoll == 0) {
            List<Integer> rolls = diceRollService.rollDice(gameData, entry.getControllerId(), sides, 2);
            firstRoll = rolls.get(0);
            secondRoll = rolls.get(1);
            entry.setXValue(firstRoll);
            entry.setEventValue(secondRoll);

            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " rolls two d" + sides + " for " + entry.getCard().getName() + ": "
                    + firstRoll + " and " + secondRoll + "."));
            ActionEvents.diceRolled(
                    gameData, entry.getControllerId(), 2, Math.max(firstRoll, secondRoll));
            ActionEvents.dieResults(
                    gameData, entry.getControllerId(), sides, firstRoll, secondRoll);
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
                throw new IllegalStateException("The chosen result was not rolled");
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        entry.setXValue(chosenRoll);
        entry.setEventValue(chosenRoll == firstRoll ? secondRoll : firstRoll);
    }
}
