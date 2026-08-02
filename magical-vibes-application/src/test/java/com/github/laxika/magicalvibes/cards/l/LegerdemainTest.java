package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegerdemainTest extends BaseCardTest {

    private void prepare() {
        harness.setHand(player1, List.of(new Legerdemain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Exchanges control of two creatures")
    void exchangesCreatures() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchanges control of two artifacts")
    void exchangesArtifacts() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Millstone());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new IcyManipulator());

        harness.castAndResolveSorcery(player1, 0, List.of(own.getId(), opponents.getId()));

        harness.assertOnBattlefield(player2, "Millstone");
        harness.assertOnBattlefield(player1, "Icy Manipulator");
    }

    @Test
    @DisplayName("Exchanges control when the opponent's permanent is the first target")
    void exchangesWithOpponentPermanentFirst() {
        prepare();
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(opponents.getId(), own.getId()));

        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does nothing when both targets have the same controller (CR 701.12b)")
    void doesNothingWhenSameController() {
        prepare();
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.castAndResolveSorcery(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Exchange does nothing when a target leaves the battlefield before resolution (CR 701.12a)")
    void fizzlesWhenTargetGone() {
        prepare();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponents = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.castSorcery(player1, 0, List.of(own.getId(), opponents.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opponents);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot pair a creature with a noncreature artifact — they share no type")
    void cannotPairCreatureWithArtifact() {
        prepare();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new Millstone());

        UUID creatureId = creature.getId();
        UUID artifactId = artifact.getId();
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(creatureId, artifactId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or creature type");
    }
}
