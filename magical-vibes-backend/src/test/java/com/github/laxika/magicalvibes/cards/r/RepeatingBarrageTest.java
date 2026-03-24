package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RepeatingBarrageTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has deal 3 damage to any target spell effect")
    void hasCorrectSpellEffect() {
        RepeatingBarrage card = new RepeatingBarrage();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);

        DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.damage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Has graveyard activated ability with raid restriction")
    void hasGraveyardAbilityWithRaid() {
        RepeatingBarrage card = new RepeatingBarrage();

        assertThat(card.getGraveyardActivatedAbilities()).hasSize(1);
        assertThat(card.getGraveyardActivatedAbilities().getFirst().getManaCost()).isEqualTo("{3}{R}{R}");
        assertThat(card.getGraveyardActivatedAbilities().getFirst().getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.RAID);
        assertThat(card.getGraveyardActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .first()
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Spell: deals 3 damage =====

    @Nested
    @DisplayName("Spell effect")
    class SpellEffectTests {

        @Test
        @DisplayName("Deals 3 damage to target player")
        void deals3DamageToPlayer() {
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("Deals 3 damage to target creature")
        void deals3DamageToCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, bearsId);
            harness.passBothPriorities();

            // 3 damage kills Grizzly Bears (2/2)
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Goes to graveyard after resolving")
        void goesToGraveyardAfterResolving() {
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Repeating Barrage"));
        }
    }

    // ===== Graveyard activated ability with raid =====

    @Nested
    @DisplayName("Graveyard activated ability")
    class GraveyardAbilityTests {

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

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Repeating Barrage"));
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Repeating Barrage"));
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

    // ===== Full loop: cast, graveyard return, re-cast =====

    @Nested
    @DisplayName("Full loop")
    class FullLoopTests {

        @Test
        @DisplayName("Can cast, return from graveyard with raid, and cast again")
        void castReturnAndRecast() {
            // First cast: deal 3 damage
            harness.setHand(player1, List.of(new RepeatingBarrage()));
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Repeating Barrage"));

            // Return from graveyard with raid
            markAttackedThisTurn();
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 3);

            harness.activateGraveyardAbility(player1, 0);
            harness.passBothPriorities();

            assertThat(gd.playerHands.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Repeating Barrage"));

            // Re-cast: deal 3 more damage
            harness.addMana(player1, ManaColor.RED, 2);
            harness.addMana(player1, ManaColor.COLORLESS, 1);

            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        }
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }
}
