package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Equilibrium.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class EquilibriumTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell without another creature does not create the trigger")
    void creatureCastWithoutLegalTargetDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new Equilibrium());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Paying {1} returns the chosen creature to its owner's hand")
    void payReturnsTargetCreature() {
        harness.addToBattlefield(player1, new Equilibrium());
        harness.addToBattlefield(player2, new HillGiant());
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.handlePermanentChosen(player1, giantId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInHand(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Declining leaves the creature on the battlefield")
    void declineLeavesCreature() {
        harness.addToBattlefield(player1, new Equilibrium());
        harness.addToBattlefield(player2, new HillGiant());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Hill Giant"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Casting a noncreature spell does not trigger")
    void noncreatureCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Equilibrium());
        harness.addToBattlefield(player2, new HillGiant());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("An opponent's creature spell does not trigger Equilibrium")
    void opponentCreatureCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new Equilibrium());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
