package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeepFreeze;
import com.github.laxika.magicalvibes.cards.d.DualShot;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.cards.j.JungleDelver;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, LightningBolt.class, GrizzlyBears.class,
        KamahlPitFighter.class, MirrorGallery.class, DeepFreeze.class, DualShot.class})
class KopalaWardenOfWavesTest extends BaseCardTest {

    

    @Nested
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, LightningBolt.class, GrizzlyBears.class})
    @DisplayName("Spell targeting tax")
    class SpellTargetingTax {

        @Test
        @DisplayName("Opponent's spell targeting a Merfolk costs {2} more")
        void opponentSpellTargetingMerfolkCostsMore() {
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

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

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 1);

            // Target a player, not a Merfolk — no tax
            harness.castInstant(player2, 0, player1.getId());

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, LightningBolt.class, MirrorGallery.class})
    @DisplayName("Two Kopalas stack")
    class TwoKopalasStack {

        @Test
        @DisplayName("Two Kopalas increase the cost by {4}")
        void twoKopalasStackCostIncrease() {
            harness.addToBattlefield(player1, new MirrorGallery());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

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
            harness.addToBattlefield(player1, new MirrorGallery());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new KopalaWardenOfWaves());
            harness.addToBattlefield(player1, new JungleDelver());

            harness.setHand(player2, List.of(new LightningBolt()));
            harness.addMana(player2, ManaColor.RED, 5);

            UUID merfolkId = harness.getPermanentId(player1, "Jungle Delver");

            harness.castInstant(player2, 0, merfolkId);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isEqualTo(0);
        }
    }

    @Nested
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, KamahlPitFighter.class, GrizzlyBears.class})
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
            int kamahlIndex = gd.playerBattlefields.get(player1.getId())
                    .indexOf(findPermanent(player1, "Kamahl, Pit Fighter"));

            // No tax — controller targeting own Merfolk
            harness.activateAbility(player1, kamahlIndex, null, merfolkId);

            assertThat(gd.stack).hasSize(1);
        }
    }

    @Test
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, DualShot.class})
    void targetingTwoProtectedMerfolkPaysTaxOnlyOnce() {
        var kopala = harness.addToBattlefieldAndReturn(player1, new KopalaWardenOfWaves());
        var delver = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castInstant(player2, 0, List.of(kopala.getId(), delver.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, DualShot.class})
    void opponentsOwnMerfolkIsNotProtected() {
        harness.addToBattlefield(player1, new KopalaWardenOfWaves());
        var delver = harness.addToBattlefieldAndReturn(player2, new JungleDelver());
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, List.of(delver.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, DeepFreeze.class, DualShot.class})
    void losingAbilitiesRemovesSpellTax() {
        var kopala = harness.addToBattlefieldAndReturn(player1, new KopalaWardenOfWaves());
        var delver = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, kopala.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deep Freeze");
        assertThat(gqs.hasLostAllAbilities(gd, kopala)).isTrue();
        harness.setHand(player2, List.of(new DualShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, List.of(delver.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }

    @Test
    @CardUsed({KopalaWardenOfWaves.class, JungleDelver.class, DeepFreeze.class, KamahlPitFighter.class})
    void losingAbilitiesRemovesActivatedAbilityTax() {
        var kopala = harness.addToBattlefieldAndReturn(player1, new KopalaWardenOfWaves());
        var delver = harness.addToBattlefieldAndReturn(player1, new JungleDelver());
        harness.setHand(player1, List.of(new DeepFreeze()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castEnchantment(player1, 0, kopala.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Deep Freeze");
        assertThat(gqs.hasLostAllAbilities(gd, kopala)).isTrue();
        harness.addToBattlefield(player2, new KamahlPitFighter());

        harness.activateAbility(player2, 0, null, delver.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
    }
}
