package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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

@CardUsed({DeputyOfAcquittals.class, KraulWarrior.class, ControlMagic.class})
class DeputyOfAcquittalsTest extends BaseCardTest {

    private void stealWithControlMagic(UUID creatureId) {
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castEnchantment(player1, 0, creatureId);
        harness.passBothPriorities();
    }

    private void castDeputy() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new DeputyOfAcquittals(), "{W}{U}");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's combat")
    void canCastDuringOpponentsCombat() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.castFromHand(player1, new DeputyOfAcquittals(), "{W}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Deputy of Acquittals");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another Deputy is a legal target even though it has the same name")
    void canReturnAnotherDeputy() {
        UUID firstDeputyId = harness.addToBattlefieldAndReturn(player1, new DeputyOfAcquittals()).getId();
        castDeputy();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstDeputyId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Deputy of Acquittals");
        harness.assertOnBattlefield(player1, "Deputy of Acquittals");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A creature controlled by you returns to its owner's hand")
    void returnsBorrowedCreatureToOwner() {
        UUID warriorId = harness.addToBattlefieldAndReturn(player2, new KraulWarrior()).getId();
        stealWithControlMagic(warriorId);
        castDeputy();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, warriorId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Kraul Warrior");
        harness.assertInHand(player2, "Kraul Warrior");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The trigger does nothing if its target was returned by another Deputy in response")
    void targetLeavesBeforeResolution() {
        UUID warriorId = harness.addToBattlefieldAndReturn(player1, new KraulWarrior()).getId();
        castDeputy();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, warriorId);

        harness.castFromHand(player1, new DeputyOfAcquittals(), "{W}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, warriorId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Kraul Warrior");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Nested
    @DisplayName("ETB may bounce another creature you control")
    @CardUsed({DeputyOfAcquittals.class, KraulWarrior.class})
    class EtbMayBounce {

        @Test
        @DisplayName("ETB prompts for a target when another creature you control exists")
        void etbPromptsForTarget() {
            harness.addToBattlefield(player1, new KraulWarrior());
            castDeputy();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        }

        @Test
        @DisplayName("Accepting bounces the chosen creature to its owner's hand")
        void acceptingMayBouncesCreature() {
            UUID warriorId = harness.addToBattlefieldAndReturn(player1, new KraulWarrior()).getId();
            castDeputy();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, warriorId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Kraul Warrior");
            harness.assertInHand(player1, "Kraul Warrior");
            harness.assertOnBattlefield(player1, "Deputy of Acquittals");
        }

        @Test
        @DisplayName("Declining does not bounce anything")
        void decliningMayDoesNotBounce() {
            UUID warriorId = harness.addToBattlefieldAndReturn(player1, new KraulWarrior()).getId();
            castDeputy();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, warriorId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Kraul Warrior");
            harness.assertOnBattlefield(player1, "Deputy of Acquittals");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({DeputyOfAcquittals.class, KraulWarrior.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("An opponent's creature is not a legal target, so the trigger never goes on the stack")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player2, new KraulWarrior());
            castDeputy();
            harness.passBothPriorities(); // resolve creature spell -> enters battlefield

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Kraul Warrior");
        }

        @Test
        @DisplayName("'Another' excludes itself, so with no other creature the trigger never goes on the stack")
        void cannotTargetItself() {
            castDeputy();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Deputy of Acquittals");
        }
    }
}
