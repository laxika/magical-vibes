package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.StampedingRhino;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaPhoenix.class, MerfolkLooter.class, RuneclawBear.class, ShivanDragon.class, StampedingRhino.class})
class MagmaPhoenixTest extends BaseCardTest {

    @CardUsed({MagmaPhoenix.class, RuneclawBear.class, ShivanDragon.class, StampedingRhino.class})
    @Nested
    @DisplayName("Death trigger")
    class DeathTriggerTests {

        @Test
        @DisplayName("When Magma Phoenix dies, death trigger goes on the stack")
        void deathTriggerGoesOnStack() {
            harness.addToBattlefield(player1, new MagmaPhoenix());
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            harness.assertInGraveyard(player1, "Magma Phoenix");

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magma Phoenix");
        }

        @Test
        @DisplayName("Resolving death trigger deals 3 damage to each player")
        void deathTriggerDamagesPlayers() {
            harness.addToBattlefield(player1, new MagmaPhoenix());
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            // Resolve the death trigger
            harness.passBothPriorities();

            // Both players take 3 damage
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Resolving death trigger deals 3 damage to each creature")
        void deathTriggerDamagesCreatures() {
            harness.addToBattlefield(player1, new MagmaPhoenix());
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            // Add a 4/4 creature for player2 that will survive 3 damage
            harness.addToBattlefield(player2, new StampedingRhino());

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            // Resolve the death trigger (deals 3 to all creatures)
            harness.passBothPriorities();

            // The 4/4 should have taken 3 damage — check it's still on battlefield
            // (SBA will handle lethal damage but 4/4 with 3 damage survives)
            harness.assertOnBattlefield(player2, "Stampeding Rhino");
            assertThat(findPermanent(player2, "Stampeding Rhino").getMarkedDamage()).isEqualTo(3);
        }

        @Test
        @DisplayName("Death trigger kills small creatures")
        void deathTriggerKillsSmallCreatures() {
            harness.addToBattlefield(player1, new MagmaPhoenix());
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            // Add a 2/2 creature for player2 that should die from 3 damage
            harness.addToBattlefield(player2, new RuneclawBear());

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            // Resolve the death trigger (deals 3 to all creatures)
            harness.passBothPriorities();

            // The 2/2 should be dead
            harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
            harness.assertInGraveyard(player2, "Runeclaw Bear");
        }
    }

    @CardUsed({MagmaPhoenix.class, RuneclawBear.class, ShivanDragon.class, StampedingRhino.class})
    @Nested
    @DisplayName("Graveyard activated ability")
    class GraveyardAbilityTests {

        @Test
        @DisplayName("Can activate graveyard ability with enough mana")
        void canActivateGraveyardAbility() {
            MagmaPhoenix phoenix = new MagmaPhoenix();
            harness.setGraveyard(player1, List.of(phoenix));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateGraveyardAbility(player1, 0);

            // Ability should be on the stack
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Magma Phoenix");
        }

        @Test
        @DisplayName("Resolving graveyard ability returns Magma Phoenix to hand")
        void resolvingGraveyardAbilityReturnsToHand() {
            MagmaPhoenix phoenix = new MagmaPhoenix();
            harness.setGraveyard(player1, List.of(phoenix));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities(); // Resolve ability

            // Phoenix should be in hand, not in graveyard
            harness.assertInHand(player1, "Magma Phoenix");
            harness.assertNotInGraveyard(player1, "Magma Phoenix");
        }

        @Test
        @DisplayName("Cannot activate graveyard ability without enough mana")
        void cannotActivateWithoutEnoughMana() {
            MagmaPhoenix phoenix = new MagmaPhoenix();
            harness.setGraveyard(player1, List.of(phoenix));
            harness.addMana(player1, ManaColor.RED, 1); // Not enough

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Graveyard ability pays mana cost")
        void graveyardAbilityPaysManaCost() {
            MagmaPhoenix phoenix = new MagmaPhoenix();
            harness.setGraveyard(player1, List.of(phoenix));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateGraveyardAbility(player1, 0);

            // Mana should be consumed
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        }

        @Test
        @DisplayName("Card without graveyard ability cannot be activated from graveyard")
        void cannotActivateNonGraveyardAbilityCard() {
            RuneclawBear bears = new RuneclawBear();
            harness.setGraveyard(player1, List.of(bears));

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no graveyard activated ability");
        }
    }

    @Test
    @DisplayName("Only the Phoenix whose ability was activated returns")
    void returnsOnlyActivatedPhoenix() {
        MagmaPhoenix source = new MagmaPhoenix();
        MagmaPhoenix other = new MagmaPhoenix();
        RuneclawBear bear = new RuneclawBear();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(source, other, bear));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other, bear);
    }

    @Test
    @DisplayName("Generic mana cannot replace the two required red mana")
    void requiresTwoRedMana() {
        harness.setGraveyard(player1, List.of(new MagmaPhoenix()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Magma Phoenix");
    }

    @Test
    @DisplayName("Returning Phoenix before its death trigger resolves does not stop the damage")
    void deathTriggerResolvesAfterPhoenixReturnsToHand() {
        harness.setHand(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new MagmaPhoenix());
        harness.addToBattlefield(player1, new RuneclawBear());
        harness.addToBattlefield(player2, new RuneclawBear());
        setupCombatWherePhoenixDies();
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Magma Phoenix");
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Runeclaw Bear");
        harness.assertInGraveyard(player2, "Runeclaw Bear");
        harness.assertInHand(player1, "Magma Phoenix");
    }

    @Test
    @CardUsed({MagmaPhoenix.class, MerfolkLooter.class, RuneclawBear.class})
    @DisplayName("An older activation cannot return Phoenix after it leaves and reenters the graveyard")
    void olderActivationCannotReturnNewGraveyardObject() {
        MagmaPhoenix phoenix = new MagmaPhoenix();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new RuneclawBear()));
        harness.setGraveyard(player1, List.of(phoenix));
        Permanent looter = harness.addToBattlefieldAndReturn(player1, new MerfolkLooter());
        looter.setSummoningSick(false);
        harness.addMana(player1, ManaColor.RED, 10);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Magma Phoenix");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInGraveyard(player1, "Magma Phoenix");
        harness.assertNotInHand(player1, "Magma Phoenix");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Magma Phoenix");
        harness.assertNotInHand(player1, "Magma Phoenix");
    }

    private void setupCombatWherePhoenixDies() {
        Permanent phoenixPerm = findPermanent(player1, "Magma Phoenix");
        phoenixPerm.setSummoningSick(false);
        phoenixPerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
