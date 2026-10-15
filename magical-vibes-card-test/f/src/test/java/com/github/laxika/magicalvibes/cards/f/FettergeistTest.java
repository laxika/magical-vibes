package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Fettergeist.class, MoorlandInquisitor.class, Island.class})
class FettergeistTest extends BaseCardTest {

    @Test
    @DisplayName("With no other creatures the cost is {0} and Fettergeist survives")
    void noOtherCreaturesCostsNothing() {
        harness.addToBattlefield(player1, new Fettergeist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Paying {1} for each other creature you control keeps Fettergeist")
    void payingPerOtherCreatureKeepsIt() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Accepting without enough mana sacrifices Fettergeist")
    void notEnoughManaSacrifices() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Declining the payment sacrifices Fettergeist")
    void decliningSacrifices() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Creatures controlled by the opponent do not raise the cost")
    void opponentCreaturesDoNotCount() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player2, new MoorlandInquisitor());
        harness.addToBattlefield(player2, new MoorlandInquisitor());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Declining even a zero payment sacrifices Fettergeist")
    void decliningZeroPaymentSacrifices() {
        harness.addToBattlefield(player1, new Fettergeist());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Fettergeist");
        harness.assertInGraveyard(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Noncreature permanents do not increase the upkeep payment")
    void noncreaturePermanentsDoNotCount() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Creatures entering after the trigger increase the payment at resolution")
    void creatureEnteringBeforeResolutionCounts() {
        harness.addToBattlefield(player1, new Fettergeist());

        advanceToUpkeep(player1);
        harness.addToBattlefield(player1, new MoorlandInquisitor());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Creatures leaving after the trigger reduce the payment at resolution")
    void creatureLeavingBeforeResolutionDoesNotCount() {
        harness.addToBattlefield(player1, new Fettergeist());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());

        advanceToUpkeep(player1);
        other.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Moorland Inquisitor");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Fettergeist");
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void noTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new Fettergeist());
        harness.addToBattlefield(player1, new MoorlandInquisitor());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fettergeist");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
