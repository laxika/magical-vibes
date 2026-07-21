package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.GameData;
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

class AbradeTest extends BaseCardTest {

    private UUID battlefieldId(com.github.laxika.magicalvibes.model.Player owner, String name) {
        return gd.playerBattlefields.get(owner.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .map(Permanent::getId)
                .findFirst().orElseThrow();
    }

    @Nested
    @DisplayName("Mode 0: Deal 3 damage to target creature")
    class DamageMode {

        @Test
        @DisplayName("Deals 3 damage to target creature, killing a 2/2")
        void deals3DamageToCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 0, battlefieldId(player2, "Grizzly Bears"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Cannot target an artifact with the damage mode")
        void cannotTargetArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID millstoneId = battlefieldId(player2, "Millstone");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, millstoneId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    class DestroyMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            harness.castInstant(player1, 0, 1, battlefieldId(player2, "Millstone"));
            harness.passBothPriorities();

            GameData gd = harness.getGameData();
            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .noneMatch(p -> p.getCard().getName().equals("Millstone"));
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Millstone"));
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature with the destroy mode")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Abrade()));
            harness.addMana(player1, ManaColor.RED, 2);

            UUID bearsId = battlefieldId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bearsId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
