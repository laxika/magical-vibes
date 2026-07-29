package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class ForsakenWastesTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can't gain life")
    void controllerCantGainLife() {
        harness.addToBattlefield(player1, new ForsakenWastes());

        harness.setHand(player1, List.of(new AngelOfMercy()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB gain life effect

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Opponent can't gain life either")
    void opponentCantGainLife() {
        harness.addToBattlefield(player1, new ForsakenWastes());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new AngelOfMercy()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB gain life effect

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Controller loses 1 life during their own upkeep")
    void controllerLosesLifeOnOwnUpkeep() {
        harness.addToBattlefield(player1, new ForsakenWastes());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent loses 1 life during their own upkeep")
    void opponentLosesLifeOnOwnUpkeep() {
        harness.addToBattlefield(player1, new ForsakenWastes());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Targeting spell's controller loses 5 life")
    void targetingSpellControllerLoses5Life() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new ForsakenWastes());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, wastes.getId());
        harness.passBothPriorities(); // resolve the becomes-target trigger
        harness.passBothPriorities(); // resolve Disenchant

        harness.assertLife(player2, 15);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Controller targeting their own enchantment loses 5 life too")
    void ownControllerTargetingLoses5Life() {
        Permanent wastes = harness.addToBattlefieldAndReturn(player1, new ForsakenWastes());

        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, wastes.getId());
        harness.passBothPriorities(); // resolve the becomes-target trigger
        harness.passBothPriorities(); // resolve Disenchant

        harness.assertLife(player1, 15);
    }
}
