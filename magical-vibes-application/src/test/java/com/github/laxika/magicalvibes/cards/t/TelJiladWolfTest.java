package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TelJiladWolf.class, Ornithopter.class, CopperMyr.class})
class TelJiladWolfTest extends BaseCardTest {

    @Test
    @DisplayName("When Tel-Jilad Wolf becomes blocked by an artifact creature, it gets +3/+3")
    void becomesBlockedByArtifactCreatureBoosts() {
        Permanent wolf = addCreatureReady(player1, new TelJiladWolf());
        addCreatureReady(player2, new Ornithopter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(3);
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("When Tel-Jilad Wolf becomes blocked by a nonartifact creature, it gets no boost")
    void becomesBlockedByNonartifactCreatureDoesNothing() {
        Permanent wolf = addCreatureReady(player1, new TelJiladWolf());
        addCreatureReady(player2, new TelJiladWolf());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Tel-Jilad Wolf becomes blocked by multiple artifact creatures, it gets only one boost")
    void becomesBlockedByMultipleArtifactCreaturesBoostsOnce() {
        Permanent wolf = addCreatureReady(player1, new TelJiladWolf());
        addCreatureReady(player2, new Ornithopter());
        addCreatureReady(player2, new Ornithopter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(3);
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Tel-Jilad Wolf's boost wears off at the end of the turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new TelJiladWolf());
        addCreatureReady(player2, new Ornithopter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isEqualTo(3);
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Blocking an artifact creature does not trigger Tel-Jilad Wolf")
    void blockingArtifactCreatureDoesNothing() {
        Permanent artifactCreature = addCreatureReady(player1, new CopperMyr());
        Permanent wolf = addCreatureReady(player2, new TelJiladWolf());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(wolf.getPowerModifier()).isZero();
        assertThat(wolf.getToughnessModifier()).isZero();
    }
}
