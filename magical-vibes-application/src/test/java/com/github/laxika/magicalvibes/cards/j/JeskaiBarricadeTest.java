package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.ArashinCleric;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JeskaiBarricade.class, ArashinCleric.class})
class JeskaiBarricadeTest extends BaseCardTest {

    private void castBarricade() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new JeskaiBarricade(), "{1}{W}");
    }

    @Test
    void canBeCastDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.castFromHand(player1, new JeskaiBarricade(), "{1}{W}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jeskai Barricade");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canReturnAnotherJeskaiBarricade() {
        UUID otherId = harness.addToBattlefieldAndReturn(player1, new JeskaiBarricade()).getId();
        castBarricade();
        harness.passBothPriorities();
        harness.passBothPriorities();
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(otherId);
        harness.handlePermanentChosen(player1, otherId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Jeskai Barricade");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(otherId));
        harness.assertOnBattlefield(player1, "Jeskai Barricade");
    }

    @Test
    void controlledCreatureReturnsToItsOwnerRatherThanController() {
        ArashinCleric cleric = new ArashinCleric();
        cleric.setOwnerId(player2.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, cleric).getId();
        castBarricade();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Arashin Cleric");
        harness.assertNotInHand(player1, "Arashin Cleric");
        harness.assertInHand(player2, "Arashin Cleric");
    }

    @Test
    void targetLeavingBattlefieldBeforeResolutionDoesNotOfferMayChoice() {
        var target = harness.addToBattlefieldAndReturn(player1, new ArashinCleric());
        castBarricade();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Arashin Cleric");
        harness.assertOnBattlefield(player1, "Jeskai Barricade");
    }

    @Nested
    @DisplayName("ETB may bounce another creature you control")
    @CardUsed({JeskaiBarricade.class, ArashinCleric.class})
    class EtbMayBounce {

        @Test
        @DisplayName("ETB prompts the may choice when another creature you control exists")
        void etbTriggersMayPrompt() {
            harness.addToBattlefield(player1, new ArashinCleric());
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        }

        @Test
        @DisplayName("Accepting bounces the chosen creature to its owner's hand")
        void acceptingMayBouncesCreature() {
            harness.addToBattlefield(player1, new ArashinCleric());
            UUID bearsId = harness.getPermanentId(player1, "Arashin Cleric");
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, bearsId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.assertNotOnBattlefield(player1, "Arashin Cleric");
            harness.assertInHand(player1, "Arashin Cleric");
            harness.assertOnBattlefield(player1, "Jeskai Barricade");
        }

        @Test
        @DisplayName("Declining does not bounce anything")
        void decliningMayDoesNotBounce() {
            harness.addToBattlefield(player1, new ArashinCleric());
            castBarricade();
            harness.passBothPriorities();
            harness.passBothPriorities();
            UUID bearsId = harness.getPermanentId(player1, "Arashin Cleric");
            harness.handlePermanentChosen(player1, bearsId);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player1, "Arashin Cleric");
            harness.assertOnBattlefield(player1, "Jeskai Barricade");
        }
    }

    @Nested
    @DisplayName("Targeting restrictions")
    @CardUsed({JeskaiBarricade.class, ArashinCleric.class})
    class TargetingRestrictions {

        @Test
        @DisplayName("An opponent's creature is not a legal target")
        void cannotTargetOpponentCreature() {
            harness.addToBattlefield(player2, new ArashinCleric());
            castBarricade();
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.stack).isEmpty();
            harness.assertOnBattlefield(player2, "Arashin Cleric");
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
