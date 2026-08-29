package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JeskaiBarricadeTest extends BaseCardTest {

    private void castBarricade() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JeskaiBarricade()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }

    @Nested
    @DisplayName("ETB may bounce another creature you control")
    class EtbMayBounce {

        @Test
        @DisplayName("ETB prompts the may choice when another creature you control exists")
        void etbTriggersMayPrompt() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        }

        @Test
        @DisplayName("Accepting bounces the chosen creature to its owner's hand")
        void acceptingMayBouncesCreature() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handlePermanentChosen(player1, bearsId);

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Jeskai Barricade");
        }

        @Test
        @DisplayName("Declining does not bounce anything")
        void decliningMayDoesNotBounce() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player1, "Jeskai Barricade");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    class TargetingRestrictions {

        @Test
        @DisplayName("An opponent's creature is not a legal target")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            castBarricade();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("'Another' excludes Jeskai Barricade itself")
        void cannotTargetItself() {
            castBarricade();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Jeskai Barricade");
        }
    }
}
