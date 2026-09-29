package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ArcaneEndeavorEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves Arcane Endeavor's two-roll choice and reuses the ordinary draw/free-cast effects. */
@Component
@RequiredArgsConstructor
public class ArcaneEndeavorEffectHandler implements NormalEffectHandlerBean {

    private final DiceRollService diceRollService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ArcaneEndeavorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int firstRoll = entry.getXValue();
        int secondRoll = entry.getEventValue();
        if (firstRoll == 0 || secondRoll == 0) {
            firstRoll = diceRollService.roll(8);
            secondRoll = diceRollService.roll(8);
            entry.setXValue(firstRoll);
            entry.setEventValue(secondRoll);

            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " rolls two d8 for " + entry.getCard().getName() + ": "
                    + firstRoll + " and " + secondRoll + "."));
            triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                    gameData, entry.getControllerId(), 2, Math.max(firstRoll, secondRoll));
            triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                    gameData, entry.getControllerId(), 8, firstRoll, secondRoll);
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
                throw new IllegalStateException("Chosen Arcane Endeavor roll was not rolled");
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        int otherRoll = chosenRoll == firstRoll ? secondRoll : firstRoll;
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            return;
        }

        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new DrawCardEffect(new Fixed(chosenRoll)),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(
                        new CardAnyOfPredicate(List.of(
                                new CardTypePredicate(CardType.INSTANT),
                                new CardTypePredicate(CardType.SORCERY))),
                        new Fixed(otherRoll))));
    }
}
