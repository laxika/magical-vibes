package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Pyroclasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Earthlink.class, BalduvianBears.class, Forest.class, Mountain.class, Pyroclasm.class})
class EarthlinkTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} at upkeep keeps Earthlink on the battlefield")
    void payAtUpkeepKeepsIt() {
        harness.addToBattlefield(player1, new Earthlink());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Earthlink");
    }

    @Test
    @DisplayName("Earthlink triggers only during its controller's upkeep")
    void opponentUpkeepDoesNotTriggerEarthlink() {
        harness.addToBattlefield(player1, new Earthlink());

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Earthlink");
    }

    @Test
    @DisplayName("Declining to pay at upkeep sacrifices Earthlink")
    void declineAtUpkeepSacrificesIt() {
        harness.addToBattlefield(player1, new Earthlink());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger → may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Earthlink");
    }

    @Test
    @DisplayName("When an opponent's creature dies, that opponent sacrifices a land of their choice")
    void opponentCreatureDiesOpponentSacrificesChosenLand() {
        harness.addToBattlefield(player1, new Earthlink());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities(); // Pyroclasm resolves → player2's bear dies
        harness.passBothPriorities(); // Earthlink trigger → land sacrifice choice

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        // The DYING creature's controller (player2), not Earthlink's controller, sacrifices.
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.context()).isInstanceOf(MultiPermanentChoiceContext.ForcedSacrifice.class);

        Permanent forest = findPermanent(player2, "Forest");
        harness.handleMultiplePermanentsChosen(player2, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Mountain");
    }

    @Test
    @DisplayName("When Earthlink's controller's own creature dies, that controller sacrifices a land")
    void ownCreatureDiesControllerSacrificesLand() {
        harness.addToBattlefield(player1, new Earthlink());
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities(); // sole land is sacrificed automatically

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Earthlink");
    }

    @Test
    @DisplayName("A dying creature's controller with no lands is unaffected")
    void noLandsMeansNothingSacrificed() {
        harness.addToBattlefield(player1, new Earthlink());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        // player2 controlled the dying creature and has no lands; Earthlink's controller keeps theirs.
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Each simultaneously dying creature creates its own land-sacrifice trigger")
    void eachSimultaneouslyDyingCreatureTriggersEarthlink() {
        harness.addToBattlefield(player1, new Earthlink());
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.addToBattlefield(player2, new BalduvianBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new Pyroclasm(), "{1}{R}");
        harness.passBothPriorities(); // Pyroclasm resolves and both bears die
        harness.passBothPriorities(); // One Earthlink trigger resolves

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(forest.getId(), mountain.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(forest.getId()));
        harness.passBothPriorities(); // resolve the second Earthlink trigger

        harness.assertNotOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player2, "Mountain");
    }
}
