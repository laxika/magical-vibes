package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Carnassid;
import com.github.laxika.magicalvibes.cards.h.Heartstone;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Awakening.class, Carnassid.class, Heartstone.class, VolrathsStronghold.class})
class AwakeningTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps all creatures and lands on every upkeep")
    void untapsAllCreaturesAndLandsOnEveryUpkeep() {
        harness.addToBattlefield(player1, new Awakening());
        Permanent creature = addCreatureReady(player1, new Carnassid());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        Permanent opponentCreature = addCreatureReady(player2, new Carnassid());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Heartstone());

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

    @Test
    @DisplayName("Triggers during both players' upkeeps")
    void triggersDuringBothPlayersUpkeeps() {
        harness.addToBattlefield(player1, new Awakening());
        Permanent player1Creature = addCreatureReady(player1, new Carnassid());
        Permanent player1Land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        Permanent player2Creature = addCreatureReady(player2, new Carnassid());
        Permanent player2Land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        player1Creature.tap();
        player1Land.tap();
        player2Creature.tap();
        player2Land.tap();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(player2Creature.isTapped()).isFalse();
        assertThat(player2Land.isTapped()).isFalse();

        player1Creature.tap();
        player1Land.tap();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(player1Creature.isTapped()).isFalse();
        assertThat(player1Land.isTapped()).isFalse();
    }
}
