package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.w.WalkingWall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaceTest extends BaseCardTest {

    @Nested
    @DisplayName("Mode 0: Destroy target artifact")
    class DestroyArtifactMode {

        @Test
        @DisplayName("Destroys target artifact")
        void destroysArtifact() {
            harness.addToBattlefield(player2, new Millstone());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent millstone = findPermanent(player2, "Millstone");

            harness.castSorcery(player1, 0, 0, millstone.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Millstone");
            harness.assertInGraveyard(player2, "Millstone");
        }

        @Test
        @DisplayName("Cannot target a non-artifact creature")
        void cannotTargetCreature() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Mode 1: Destroy target creature with defender")
    class DestroyDefenderMode {

        @Test
        @DisplayName("Destroys target creature with defender")
        void destroysDefender() {
            harness.addToBattlefield(player2, new WalkingWall());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent wall = findPermanent(player2, "Walking Wall");

            harness.castSorcery(player1, 0, 1, wall.getId());
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Walking Wall");
            harness.assertInGraveyard(player2, "Walking Wall");
        }

        @Test
        @DisplayName("Cannot target a creature without defender")
        void cannotTargetCreatureWithoutDefender() {
            harness.addToBattlefield(player2, new GrizzlyBears());
            harness.setHand(player1, List.of(new Deface()));
            harness.addMana(player1, ManaColor.RED, 1);

            Permanent bears = findPermanent(player2, "Grizzly Bears");

            assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, bears.getId()))
                    .isInstanceOf(IllegalStateException.class);
        }
    }
}
