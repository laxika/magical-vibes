package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorHatchling;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CherishedHatchling.class, GrizzlyBears.class, RaptorHatchling.class, Shock.class})
class CherishedHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("After it dies, Dinosaur creature spells can be cast during the opponent's turn")
    void grantsFlashToDinosaursThisTurn() {
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RaptorHatchling(), "{1}{R}");

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Raptor Hatchling"));
    }

    @Test
    @DisplayName("The death ability does not grant flash to non-Dinosaur creature spells")
    void doesNotGrantFlashToNonDinosaurs() {
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Dinosaur cast this turn gains the optional enter-the-battlefield fight ability")
    void DinosaurGainsFightAbility() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new RaptorHatchling(), "{1}{R}");

        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Raptor Hatchling");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The entering Dinosaur can decline to fight after choosing a target")
    void canDeclineFight() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.castFromHand(player1, new RaptorHatchling(), "{1}{R}");
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(findPermanent(player1, "Raptor Hatchling").getMarkedDamage()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Dinosaur put onto the battlefield without being cast does not gain a fight ability")
    void enteringWithoutBeingCastDoesNotGrantFight() {
        addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.enterBattlefieldAndReturn(player1, new RaptorHatchling());

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's Dinosaur spell does not gain the fight ability")
    void opponentsDinosaurDoesNotGainFight() {
        addCreatureReady(player1, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new RaptorHatchling(), "{1}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Raptor Hatchling");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Non-Dinosaur spells cast by the controller do not gain a fight ability")
    void nonDinosaurDoesNotGainFight() {
        addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Both parts of the death ability form one triggered ability on the stack")
    void deathAbilityUsesOneStackEntry() {
        Permanent hatchling = addCreatureReady(player1, new CherishedHatchling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cherished Hatchling");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Dinosaur cast this turn gains the optional fight ability")
    void multipleDinosaursGainFight() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        destroyHatchlingAndResolveDeathTriggers();

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new RaptorHatchling(), "{1}{R}");
            resolveAllTriggers();
            harness.handlePermanentChosen(player1, target.getId());
            resolveAllTriggers();
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(countPermanents(player1, "Raptor Hatchling")).isEqualTo(2);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An entering Dinosaur cannot fight itself when no other creature exists")
    void cannotFightItself() {
        destroyHatchlingAndResolveDeathTriggers();

        harness.castFromHand(player1, new RaptorHatchling(), "{1}{R}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Raptor Hatchling");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void destroyHatchlingAndResolveDeathTriggers() {
        Permanent hatchling = addCreatureReady(player1, new CherishedHatchling());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, hatchling.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Cherished Hatchling");
    }
}
