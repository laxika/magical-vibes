package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InspiringPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, Inspiring Paladin has first strike and grants it to countered creatures you control")
    void grantsFirstStrikeDuringControllerTurn() {
        Permanent paladin = addPaladin(player1);
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent uncounteredCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, uncounteredCreature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("During an opponent's turn, Inspiring Paladin's first-strike abilities do not apply")
    void doesNotGrantFirstStrikeDuringOpponentTurn() {
        Permanent paladin = addPaladin(player1);
        Permanent counteredCreature = addCreatureReady(player1, new GrizzlyBears());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The counter-based first strike grant is removed when the counter is removed")
    void counterBasedGrantTracksCounters() {
        addPaladin(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addPaladin(Player player) {
        return addCreatureReady(player, new InspiringPaladin());
    }
}
