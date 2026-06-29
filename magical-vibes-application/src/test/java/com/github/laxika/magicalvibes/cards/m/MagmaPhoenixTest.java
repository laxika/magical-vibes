package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagmaPhoenixTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has ON_DEATH MassDamageEffect dealing 3 damage to each creature and player")
    void hasDeathTrigger() {
        MagmaPhoenix card = new MagmaPhoenix();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MassDamageEffect.class);
        MassDamageEffect effect = (MassDamageEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(effect.damage()).isEqualTo(3);
        assertThat(effect.damagesPlayers()).isTrue();
    }

    @Test
    @DisplayName("Has graveyard activated ability with ReturnCardFromGraveyardEffect")
    void hasGraveyardAbility() {
        MagmaPhoenix card = new MagmaPhoenix();

        assertThat(card.getGraveyardActivatedAbilities()).hasSize(1);
        assertThat(card.getGraveyardActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}{R}{R}");
        assertThat(card.getGraveyardActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Death trigger =====

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

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Magma Phoenix"));

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
            GrizzlyBears toughCreature = new GrizzlyBears();
            toughCreature.setPower(4);
            toughCreature.setToughness(4);
            Permanent toughPerm = new Permanent(toughCreature);
            toughPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(toughPerm);

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            // Resolve the death trigger (deals 3 to all creatures)
            harness.passBothPriorities();

            // The 4/4 should have taken 3 damage — check it's still on battlefield
            // (SBA will handle lethal damage but 4/4 with 3 damage survives)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Death trigger kills small creatures")
        void deathTriggerKillsSmallCreatures() {
            harness.addToBattlefield(player1, new MagmaPhoenix());
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            // Add a 2/2 creature for player2 that should die from 3 damage
            GrizzlyBears smallCreature = new GrizzlyBears();
            Permanent smallPerm = new Permanent(smallCreature);
            smallPerm.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(smallPerm);

            setupCombatWherePhoenixDies();
            harness.passBothPriorities(); // Combat damage — Phoenix dies

            // Resolve the death trigger (deals 3 to all creatures)
            harness.passBothPriorities();

            // The 2/2 should be dead
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        }
    }

    // ===== Graveyard activated ability =====

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
            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Magma Phoenix"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Magma Phoenix"));
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
            GrizzlyBears bears = new GrizzlyBears();
            harness.setGraveyard(player1, List.of(bears));

            assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no graveyard activated ability");
        }
    }

    // ===== Helpers =====

    private void setupCombatWherePhoenixDies() {
        Permanent phoenixPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Magma Phoenix"))
                .findFirst().orElseThrow();
        phoenixPerm.setSummoningSick(false);
        phoenixPerm.setAttacking(true);

        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent blockerPerm = new Permanent(bigCreature);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
