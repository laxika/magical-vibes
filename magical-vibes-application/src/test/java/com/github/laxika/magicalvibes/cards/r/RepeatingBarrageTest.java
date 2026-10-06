package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.j.JaceCunningCastaway;
import com.github.laxika.magicalvibes.cards.n.NestRobber;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RepeatingBarrage.class, NestRobber.class, JaceCunningCastaway.class, RummagingGoblin.class})
class RepeatingBarrageTest extends BaseCardTest {

    @Nested
    @DisplayName("Spell effect")
    @CardUsed({RepeatingBarrage.class, NestRobber.class, JaceCunningCastaway.class})
    class SpellEffectTests {

        @Test
        void deals3DamageToPlaneswalker() {
            harness.addToBattlefield(player2, new JaceCunningCastaway());
            UUID jaceId = harness.getPermanentId(player2, "Jace, Cunning Castaway");
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 3);

            harness.castAndResolveSorcery(player1, 0, jaceId);

            harness.assertNotOnBattlefield(player2, "Jace, Cunning Castaway");
            harness.assertInGraveyard(player2, "Jace, Cunning Castaway");
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Deals 3 damage to target player")
        void deals3DamageToPlayer() {
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Deals 3 damage to target creature")
        void deals3DamageToCreature() {
            harness.addToBattlefield(player2, new NestRobber());
            UUID robberId = harness.getPermanentId(player2, "Nest Robber");

            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveSorcery(player1, 0, robberId);

            // 3 damage kills Nest Robber (2/1)
            harness.assertNotOnBattlefield(player2, "Nest Robber");
            harness.assertInGraveyard(player2, "Nest Robber");
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            harness.assertInGraveyard(player1, "Repeating Barrage");
        }
    }

    @Nested
    @DisplayName("Graveyard activated ability")
    @CardUsed({RepeatingBarrage.class, NestRobber.class, RummagingGoblin.class})
    class GraveyardAbilityTests {

        @Test
        void returnsOnlyTheActivatedCopy() {
            RepeatingBarrage activated = new RepeatingBarrage();
            RepeatingBarrage other = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(activated, other));
            harness.addMana(player1, ManaColor.RED, 5);
            markAttackedThisTurn();

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId())).contains(activated).doesNotContain(other);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        }

        @Test
        void canActivateInResponseToASpellAfterAttacking() {
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.setGraveyard(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 8);
            markAttackedThisTurn();

            harness.castSorcery(player1, 0, player2.getId());
            harness.activateGraveyardAbility(player1, 0);
            assertThat(gd.stack).hasSize(2);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Repeating Barrage");
            harness.assertLife(player2, 20);
            harness.passBothPriorities();
            harness.assertLife(player2, 17);
        }

        @Test
        void opponentsAttackDoesNotEnableRaid() {
            harness.setGraveyard(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 5);
            gd.playersDeclaredAttackersThisTurn.add(player2.getId());

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Raid");
        }

        @Test
        void olderActivationDoesNotReturnCardAfterItLeavesAndReentersGraveyard() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setHand(player1, List.of());
            harness.setGraveyard(player1, List.of(barrage));
            harness.setLibrary(player1, List.of(new NestRobber()));
            harness.addToBattlefield(player1, new RummagingGoblin());
            gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
            harness.addMana(player1, ManaColor.RED, 10);
            markAttackedThisTurn();

            harness.activateGraveyardAbility(player1, 0);
            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();
            harness.assertInHand(player1, "Repeating Barrage");
            assertThat(gd.stack).hasSize(1);

            harness.activateAbility(player1, 0, null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.assertInGraveyard(player1, "Repeating Barrage");
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Repeating Barrage");
            harness.assertNotInHand(player1, "Repeating Barrage");
        }

        @Test
        @DisplayName("Can activate graveyard ability when raid is met")
        void canActivateWithRaid() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(barrage));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            markAttackedThisTurn();

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
            assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Repeating Barrage");
        }

        @Test
        @DisplayName("Resolving graveyard ability returns Repeating Barrage to hand")
        void resolvingReturnsToHand() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(barrage));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            markAttackedThisTurn();

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Repeating Barrage");
            harness.assertNotInGraveyard(player1, "Repeating Barrage");
        }

        @Test
        @DisplayName("Cannot activate graveyard ability without raid")
        void cannotActivateWithoutRaid() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(barrage));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Raid");
        }

        @Test
        @DisplayName("Cannot activate graveyard ability without enough mana")
        void cannotActivateWithoutEnoughMana() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(barrage));
            harness.addMana(player1, ManaColor.RED, 1); // Not enough
            markAttackedThisTurn();

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Graveyard ability pays mana cost")
        void graveyardAbilityPaysManaCost() {
            RepeatingBarrage barrage = new RepeatingBarrage();
            harness.setGraveyard(player1, List.of(barrage));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);
            markAttackedThisTurn();

            harness.activateGraveyardAbility(player1, 0);

            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
            assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Full loop")
    @CardUsed({RepeatingBarrage.class})
    class FullLoopTests {

        @Test
        @DisplayName("Can cast, return from graveyard with raid, and cast again")
        void castReturnAndRecast() {
            // First cast: deal 3 damage
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            harness.assertInGraveyard(player1, "Repeating Barrage");

            // Return from graveyard with raid
            markAttackedThisTurn();
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            harness.assertInHand(player1, "Repeating Barrage");

            // Re-cast: deal 3 more damage
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castAndResolveSorcery(player1, 0, player2.getId());

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        }
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }
}
