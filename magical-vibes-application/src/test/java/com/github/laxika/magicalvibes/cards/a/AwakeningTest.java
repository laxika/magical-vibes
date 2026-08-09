package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all creatures and lands on every upkeep")
    void untapsAllCreaturesAndLandsOnEveryUpkeep() {
        harness.addToBattlefield(player1, new Awakening());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        creature.tap();
        land.tap();
        opponentCreature.tap();
        opponentLand.tap();
        artifact.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isFalse();
        assertThat(land.isTapped()).isFalse();
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentLand.isTapped()).isFalse();
        assertThat(artifact.isTapped()).isTrue();
    }
}
