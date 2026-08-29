package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeapfrogTest extends BaseCardTest {

    @BeforeEach
    void setUpTest() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Has flying after its controller casts an instant")
    void gainsFlyingAfterInstant() {
        Permanent leapfrog = addCreatureReady(player1, new Leapfrog());
        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isFalse();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Has flying after its controller casts a sorcery")
    void gainsFlyingAfterSorcery() {
        Permanent leapfrog = addCreatureReady(player1, new Leapfrog());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorcery(player1, 0, 0);

        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not have flying after its controller casts a creature")
    void doesNotGainFlyingAfterCreature() {
        Permanent leapfrog = addCreatureReady(player1, new Leapfrog());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying wears off at the end of the turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent leapfrog = addCreatureReady(player1, new Leapfrog());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, leapfrog, Keyword.FLYING)).isFalse();
    }
}
