package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KeldonBattlewagon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({CitadelOfPain.class, RhysticCave.class, KeldonBattlewagon.class})
class CitadelOfPainTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Deals damage equal to the end-step player's untapped lands")
    void dealsDamageEqualToUntappedLands() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        Permanent tappedLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLife(player1, 20);
        tappedLand.tap();

        advanceToEndStepTrigger(player1);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Deals damage to the player whose end step it is")
    void damagesEndStepPlayerNotCitadelController() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        harness.addToBattlefield(player2, new RhysticCave());
        harness.addToBattlefield(player2, new RhysticCave());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepTrigger(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals no damage when the end-step player's lands are tapped")
    void dealsNoDamageForTappedLands() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.setLife(player1, 20);
        firstLand.tap();
        secondLand.tap();

        advanceToEndStepTrigger(player1);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not count untapped nonland permanents")
    void doesNotCountNonlandPermanents() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.addToBattlefield(player1, new KeldonBattlewagon());
        harness.setLife(player1, 20);

        advanceToEndStepTrigger(player1);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Counts untapped lands when the trigger resolves")
    void countsUntappedLandsAtResolution() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        Permanent landTappedInResponse = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        harness.addToBattlefield(player1, new RhysticCave());
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        landTappedInResponse.tap();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }
}
