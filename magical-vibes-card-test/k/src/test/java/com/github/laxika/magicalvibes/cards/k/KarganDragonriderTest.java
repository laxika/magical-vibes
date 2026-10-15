package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.SparktongueDragon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KarganDragonrider.class, SparktongueDragon.class})
class KarganDragonriderTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying when its controller controls a Dragon")
    void hasFlyingWithDragon() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        harness.addToBattlefield(player1, new SparktongueDragon());

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not have flying without a Dragon")
    void noFlyingWithoutDragon() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Dragon does not grant flying")
    void opponentDragonDoesNotCount() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        harness.addToBattlefield(player2, new SparktongueDragon());

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Loses flying when the Dragon leaves the battlefield")
    void losesFlyingWhenDragonLeaves() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new SparktongueDragon());
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dragon);

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Gains flying immediately when a Dragon is added to its controller's battlefield")
    void gainsFlyingWhenDragonArrives() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player1, new SparktongueDragon());

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Keeps flying until the last controlled Dragon leaves")
    void keepsFlyingWithRemainingDragon() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        Permanent firstDragon = harness.addToBattlefieldAndReturn(player1, new SparktongueDragon());
        Permanent secondDragon = harness.addToBattlefieldAndReturn(player1, new SparktongueDragon());
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstDragon);
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondDragon);
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A Dragon card in hand does not grant flying")
    void dragonInHandDoesNotCount() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        harness.setHand(player1, java.util.List.of(new SparktongueDragon()));

        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Checks the Dragonrider's current controller when control changes")
    void usesCurrentController() {
        Permanent dragonrider = harness.addToBattlefieldAndReturn(player1, new KarganDragonrider());
        harness.addToBattlefield(player1, new SparktongueDragon());
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dragonrider);
        gd.playerBattlefields.get(player2.getId()).add(dragonrider);
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isFalse();

        harness.addToBattlefield(player2, new SparktongueDragon());
        assertThat(gqs.hasKeyword(gd, dragonrider, Keyword.FLYING)).isTrue();
    }
}
