package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RakdosCharmTest extends BaseCardTest {

    private void addBR() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    @Nested
    @DisplayName("Mode 0: Exile target player's graveyard")
    class ExileGraveyardMode {

        @Test
        @DisplayName("Empties the targeted player's graveyard")
        void exilesGraveyard() {
            harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            harness.castInstant(player1, 0, 0, player2.getId());
            harness.passBothPriorities();

            assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            UUID targetId = harness.getPermanentId(player2, "Millstone");
            harness.castInstant(player1, 0, 1, targetId);
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new Millstone());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
            assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 2: Each creature deals 1 damage to its controller")
    class DamageControllersMode {

        @Test
        @DisplayName("Each creature damages its own controller")
        void damagesControllers() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.addToBattlefield(player2, new HillGiant());
            harness.setHand(player1, List.of(new RakdosCharm()));
            addBR();

            int p1Life = gd.playerLifeTotals.get(player1.getId());
            int p2Life = gd.playerLifeTotals.get(player2.getId());

            harness.castInstant(player1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(p1Life - 1);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(p2Life - 2);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Hill Giant");
        }
    }
}
