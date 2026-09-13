package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.Castle;
import com.github.laxika.magicalvibes.cards.f.Fervor;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.StarfieldOfNyx;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Castle.class, Fervor.class, GiantCockroach.class, GloriousAnthem.class, Knighthood.class,
        StarfieldOfNyx.class})
class KnighthoodTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain first strike")
    void ownCreaturesGainFirstStrike() {
        Permanent creature = addCreatureReady(player1, new GiantCockroach());
        harness.addToBattlefield(player1, new Knighthood());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering under your control gain first strike")
    void laterCreaturesGainFirstStrike() {
        harness.addToBattlefield(player1, new Knighthood());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new GiantCockroach());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain first strike")
    void opponentCreaturesDoNotGainFirstStrike() {
        Permanent opponentCreature = addCreatureReady(player2, new GiantCockroach());
        harness.addToBattlefield(player1, new Knighthood());

        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Knighthood has first strike when it becomes a creature")
    void creatureKnighthoodGainsFirstStrike() {
        harness.addToBattlefield(player1, new StarfieldOfNyx());
        harness.addToBattlefield(player1, new Fervor());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player1, new Castle());
        Permanent knighthood = harness.addToBattlefieldAndReturn(player1, new Knighthood());

        assertThat(gqs.isCreature(gd, knighthood)).isTrue();
        assertThat(gqs.hasKeyword(gd, knighthood, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("First strike is removed when Knighthood leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent creature = addCreatureReady(player1, new GiantCockroach());
        Permanent knighthood = harness.addToBattlefieldAndReturn(player1, new Knighthood());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .remove(knighthood);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }
}
