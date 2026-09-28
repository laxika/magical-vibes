package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.WildEndeavorEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

/** Resolves Wild Endeavor's two-roll choice and reuses the ordinary token/search effects. */
@Component
@RequiredArgsConstructor
public class WildEndeavorEffectHandler implements NormalEffectHandlerBean {

    private final D4RollService d4RollService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WildEndeavorEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int firstRoll = entry.getXValue();
        int secondRoll = entry.getEventValue();
        if (firstRoll == 0 || secondRoll == 0) {
            firstRoll = d4RollService.roll(gameData, entry.getControllerId());
            secondRoll = d4RollService.roll(gameData, entry.getControllerId());
            entry.setXValue(firstRoll);
            entry.setEventValue(secondRoll);

            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                    + " rolls two d4 for " + entry.getCard().getName() + ": "
                    + firstRoll + " and " + secondRoll + "."));
            triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                    gameData, entry.getControllerId(), 2, Math.max(firstRoll, secondRoll));
            triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                    gameData, entry.getControllerId(), 4, firstRoll, secondRoll);
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
                throw new IllegalStateException("Chosen Wild Endeavor roll was not rolled");
            }
        }

        gameData.rerunCurrentEffectAfterInteraction = false;
        int landCount = chosenRoll == firstRoll ? secondRoll : firstRoll;
        int effectIndex = entry.getEffectsToResolve().indexOf(effect);
        if (effectIndex < 0) {
            return;
        }
        entry.insertEffectsToResolve(effectIndex + 1, List.of(
                new CreateTokenEffect(chosenRoll, "Beast", 3, 3, CardColor.GREEN,
                        List.of(CardSubtype.BEAST), Set.of(), Set.of()),
                new SearchLibraryEffect(new Fixed(landCount), CardPredicateUtils.basicLand(),
                        LibrarySearchDestination.BATTLEFIELD_TAPPED)));
    }
}
