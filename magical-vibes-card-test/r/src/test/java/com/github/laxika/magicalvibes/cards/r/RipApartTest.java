package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.Bookwurm;
import com.github.laxika.magicalvibes.cards.k.KasminaEnigmaSage;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RipApart.class, GrizzlyBears.class, GloriousAnthem.class, Millstone.class,
        Bookwurm.class, KasminaEnigmaSage.class})
class RipApartTest extends BaseCardTest {

    private void castSpell(int modeIndex, Permanent target) {
        harness.setHand(player1, List.of(new RipApart()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, modeIndex, target.getId());
    }

    @Nested
    @DisplayName("Mode 0: Deal 3 damage to target creature or planeswalker")
    @CardUsed({RipApart.class, GrizzlyBears.class, Millstone.class, Bookwurm.class, KasminaEnigmaSage.class})
    class DamageMode {

        @Test
        @DisplayName("The damage mode cannot target a player")
        void cannotTargetPlayer() {
            harness.setHand(player1, List.of(new RipApart()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Marks exactly three damage on a surviving creature")
        void marksThreeDamage() {
            Permanent wurm = harness.addToBattlefieldAndReturn(player2, new Bookwurm());

            castSpell(0, wurm);

            harness.assertOnBattlefield(player2, "Bookwurm");
            assertThat(wurm.getMarkedDamage()).isEqualTo(3);
        }

        @Test
        @DisplayName("Removes exactly three loyalty from a surviving planeswalker")
        void damagesPlaneswalker() {
            Permanent kasmina = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
            kasmina.setCounterCount(CounterType.LOYALTY, 5);

            castSpell(0, kasmina);

            harness.assertOnBattlefield(player2, "Kasmina, Enigma Sage");
            assertThat(kasmina.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            assertThat(kasmina.getMarkedDamage()).isZero();
        }

        @Test
        @DisplayName("A planeswalker with insufficient loyalty goes to the graveyard")
        void killsPlaneswalker() {
            Permanent kasmina = harness.addToBattlefieldAndReturn(player2, new KasminaEnigmaSage());
            kasmina.setCounterCount(CounterType.LOYALTY, 2);

            castSpell(0, kasmina);

            harness.assertNotOnBattlefield(player2, "Kasmina, Enigma Sage");
            harness.assertInGraveyard(player2, "Kasmina, Enigma Sage");
        }

        @Test
        @DisplayName("Deals 3 damage to target creature")
        void dealsDamageToCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

            castSpell(0, bears);

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target an artifact with the damage mode")
        void cannotTargetArtifact() {
            Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
            harness.setHand(player1, List.of(new RipApart()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, millstone.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact or enchantment")
    @CardUsed({RipApart.class, Millstone.class, GloriousAnthem.class, GrizzlyBears.class})
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());

            castSpell(1, millstone);

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Destroys target enchantment")
        void destroysEnchantment() {
            Permanent gloriousAnthem = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());

            castSpell(1, gloriousAnthem);

            harness.assertNotOnBattlefield(player2, "Glorious Anthem");
            harness.assertInGraveyard(player2, "Glorious Anthem");
        }

        @Test
        @DisplayName("Cannot target a creature with the destroy mode")
        void cannotTargetCreature() {
            Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RipApart()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.addMana(player1, ManaColor.WHITE, 1);

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
