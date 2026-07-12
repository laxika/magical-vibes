package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HammerOfBogardanTest extends BaseCardTest {

    // ===== Casting as a spell (3 damage to any target) =====

    @Nested
    @DisplayName("Spell — deals 3 damage to any target")
    class SpellTests {

        @Test
        @DisplayName("Deals 3 damage to target player")
        void deals3DamageToPlayer() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new HammerOfBogardan()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Deals 3 damage to a creature, destroying a 2/2")
        void deals3DamageToCreatureDestroysIt() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new HammerOfBogardan()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            harness.castSorcery(player1, 0, targetId);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Goes to graveyard after resolving as a spell")
        void goesToGraveyardAfterResolving() {
            harness.setLife(player2, 20);
            harness.setHand(player1, List.of(new HammerOfBogardan()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castSorcery(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Hammer of Bogardan"));
        }
    }

    // ===== Graveyard activated ability ({2}{R}{R}{R}, only during your upkeep) =====

    @Nested
    @DisplayName("Graveyard activated ability")
    class GraveyardAbilityTests {

        @Test
        @DisplayName("Resolving during upkeep returns Hammer of Bogardan to hand")
        void returnsToHandDuringUpkeep() {
            HammerOfBogardan hammer = new HammerOfBogardan();
            harness.setGraveyard(player1, List.of(hammer));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.stack).hasSize(1);
            StackEntry entry = gd.stack.getFirst();
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Hammer of Bogardan"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Hammer of Bogardan"));
        }

        @Test
        @DisplayName("Cannot activate outside your upkeep")
        void cannotActivateOutsideUpkeep() {
            HammerOfBogardan hammer = new HammerOfBogardan();
            harness.setGraveyard(player1, List.of(hammer));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot activate during opponent's upkeep")
        void cannotActivateDuringOpponentsUpkeep() {
            HammerOfBogardan hammer = new HammerOfBogardan();
            harness.setGraveyard(player1, List.of(hammer));
            harness.addMana(player1, ManaColor.RED, 3);
            harness.addMana(player1, ManaColor.COLORLESS, 2);
            harness.forceActivePlayer(player2);
            harness.forceStep(TurnStep.UPKEEP);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Cannot activate without enough mana")
        void cannotActivateWithoutEnoughMana() {
            HammerOfBogardan hammer = new HammerOfBogardan();
            harness.setGraveyard(player1, List.of(hammer));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.UPKEEP);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
