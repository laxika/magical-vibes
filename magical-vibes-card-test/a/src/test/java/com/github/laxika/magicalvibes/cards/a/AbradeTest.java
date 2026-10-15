package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.w.WallOfForgottenPharaohs;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Abrade.class, GrizzlyBears.class, Millstone.class, WallOfForgottenPharaohs.class})
class AbradeTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Deal 3 damage to target creature")
    @CardUsed({Abrade.class, GrizzlyBears.class, Millstone.class, WallOfForgottenPharaohs.class})
    class DamageMode {

        @Test
        @DisplayName("Deals 3 damage to target creature, killing a 2/2")
        void deals3DamageToCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Cannot target an artifact with the damage mode")
        void cannotTargetArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID millstoneId = harness.getPermanentId(player2, "Millstone");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, millstoneId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Damage mode marks exactly 3 damage on an artifact creature without destroying it")
        void damagesArtifactCreatureWithoutDestroyingIt() {
            harness.addToBattlefield(player2, new WallOfForgottenPharaohs());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 0, harness.getPermanentId(player2, "Wall of Forgotten Pharaohs"));
            harness.passBothPriorities();

            harness.assertOnBattlefield(player2, "Wall of Forgotten Pharaohs");
            assertThat(gd.playerBattlefields.get(player2.getId()).getFirst().getMarkedDamage()).isEqualTo(3);
            harness.assertInGraveyard(player1, "Abrade");
        }

        @Test
        @DisplayName("Damage mode cannot target a player")
        void cannotTargetPlayer() {
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, player2.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    @CardUsed({Abrade.class, GrizzlyBears.class, Millstone.class, WallOfForgottenPharaohs.class})
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player2, "Millstone"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with the destroy mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("Destroy mode destroys an artifact creature you control regardless of its toughness")
        void destroysOwnArtifactCreature() {
            harness.addToBattlefield(player1, new WallOfForgottenPharaohs());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 1, harness.getPermanentId(player1, "Wall of Forgotten Pharaohs"));
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Wall of Forgotten Pharaohs");
            harness.assertInGraveyard(player1, "Wall of Forgotten Pharaohs");
            harness.assertInGraveyard(player1, "Abrade");
        }
    }
}
