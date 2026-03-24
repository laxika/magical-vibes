package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KopalaWardenOfWavesTest extends BaseCardTest {

    @Nested
    @DisplayName("Spell targeting tax")
    class SpellTargetingTax {

        @Test
        @DisplayName("Opponent's spell targeting a Merfolk costs {2} more")
        void opponentSpellTargetingMerfolkCostsMore() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            // {R} is not enough — needs {2}{R} with Kopala
            assertThatThrownBy(() -> harness.castInstant(player2, 0, merfolkId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Opponent can cast spell targeting Merfolk with enough mana")
        void opponentCanCastSpellTargetingMerfolkWithEnoughMana() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            harness.castInstant(player2, 0, merfolkId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's spell targeting a non-Merfolk is not taxed")
        void opponentSpellTargetingNonMerfolkNotTaxed() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            // {R} is enough — Grizzly Bears is not a Merfolk
            harness.castInstant(player2, 0, bearId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Controller's own spells targeting own Merfolk are not taxed")
        void controllerOwnSpellsNotTaxed() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.setHand(player1, List.of(new LightningBolt()));
            harness.addMana(player1, ManaColor.RED, 1);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            // {R} is enough — Kopala only taxes opponents
            harness.castInstant(player1, 0, merfolkId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Kopala protects itself (is also a Merfolk)")
        void kopalaProtectsItself() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            UUID kopalaId = harness.getPermanentId(player1, "Kopala, Warden of Waves");

            // {R} is not enough — Kopala is a Merfolk
            assertThatThrownBy(() -> harness.castInstant(player2, 0, kopalaId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Spell targeting a player is not taxed")
        void spellTargetingPlayerNotTaxed() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            // Target a player, not a Merfolk — no tax
            harness.castInstant(player2, 0, player1.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Two Kopalas stack")
    class TwoKopalasStack {

        @Test
        @DisplayName("Two Kopalas increase the cost by {4}")
        void twoKopalasStackCostIncrease() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 3);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            // {2}{R} is not enough — needs {4}{R} with two Kopalas
            assertThatThrownBy(() -> harness.castInstant(player2, 0, merfolkId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Two Kopalas — can cast with enough mana")
        void twoKopalasCanCastWithEnoughMana() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.forceActivePlayer(player2);
            harness.forceStep(gd.currentStep);
            harness.clearPriorityPassed();
            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 5);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            harness.castInstant(player2, 0, merfolkId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Activated ability targeting tax")
    class ActivatedAbilityTargetingTax {

        @Test
        @DisplayName("Opponent's activated ability targeting Merfolk costs {2} more")
        void opponentAbilityTargetingMerfolkCostsMore() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            // Kamahl, Pit Fighter has Haste and "{T}: Deal 3 damage to any target" (no mana cost)
            harness.addToBattlefield(player2, new KamahlPitFighter());

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            // Kamahl's ability is {T} only (no mana), but Kopala adds {2}.
            // Player2 has no mana, so should fail.
            assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, merfolkId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Opponent's activated ability targeting Merfolk succeeds with enough mana")
        void opponentAbilityTargetingMerfolkSucceedsWithMana() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.addToBattlefield(player2, new KamahlPitFighter());
            harness.addMana(player2, ManaColor.RED, 2);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            harness.activateAbility(player2, 0, null, merfolkId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }

        @Test
        @DisplayName("Opponent's activated ability targeting non-Merfolk is not taxed")
        void opponentAbilityTargetingNonMerfolkNotTaxed() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new GrizzlyBears());

            harness.addToBattlefield(player2, new KamahlPitFighter());

            UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");

            // No mana needed — ability has no mana cost and target is not a Merfolk
            harness.activateAbility(player2, 0, null, bearId);

            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("Controller's own activated ability targeting own Merfolk is not taxed")
        void controllerOwnAbilityNotTaxed() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            // Put Kamahl on same side as Kopala — should not be taxed
            harness.addToBattlefield(player1, new KamahlPitFighter());

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            // Find Kamahl's index on player1's battlefield
            int kamahlIndex = findPermanentIndex(player1, "Kamahl, Pit Fighter");

            // No tax — controller targeting own Merfolk
            harness.activateAbility(player1, kamahlIndex, null, merfolkId);

            assertThat(gd.stack).hasSize(1);
        }
    }

    private int findPermanentIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        var battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
