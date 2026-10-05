package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InspiringPaladin.class, BearCub.class})
class InspiringPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, Inspiring Paladin has first strike and grants it to countered creatures you control")
    void grantsFirstStrikeDuringControllerTurn() {
        Permanent paladin = addPaladin(player1);
        Permanent counteredCreature = addCreatureReady(player1, new BearCub());
        Permanent uncounteredCreature = addCreatureReady(player1, new BearCub());
        Permanent opponentCreature = addCreatureReady(player2, new BearCub());
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
        Permanent counteredCreature = addCreatureReady(player1, new BearCub());
        counteredCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, counteredCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The counter-based first strike grant is removed when the counter is removed")
    void counterBasedGrantTracksCounters() {
        addPaladin(player1);
        Permanent creature = addCreatureReady(player1, new BearCub());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Counters other than +1/+1 counters do not qualify for first strike")
    void otherCounterTypesDoNotQualify() {
        addPaladin(player1);
        Permanent creature = addCreatureReady(player1, new BearCub());
        creature.setCounterCount(CounterType.CHARGE, 1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Both first-strike abilities track the active player's turn")
    void grantsTrackTurnChanges() {
        Permanent paladin = addPaladin(player2);
        Permanent creature = addCreatureReady(player2, new BearCub());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, paladin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The first-strike grant ends when Inspiring Paladin leaves the battlefield")
    void grantEndsWhenPaladinLeaves() {
        Permanent paladin = addPaladin(player1);
        Permanent creature = addCreatureReady(player1, new BearCub());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(paladin);
        gd.playerGraveyards.get(player1.getId()).add(paladin.getCard());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addPaladin(Player player) {
        return addCreatureReady(player, new InspiringPaladin());
    }
}
