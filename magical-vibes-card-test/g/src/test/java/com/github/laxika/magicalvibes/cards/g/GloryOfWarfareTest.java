package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.q.QasaliPridemage;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloryOfWarfare.class, QasaliPridemage.class})
class GloryOfWarfareTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +2/+0 during your turn")
    void plusTwoZeroDuringYourTurn() {
        Permanent creature = addCreatureReady(player1, new QasaliPridemage());
        harness.addToBattlefield(player1, new GloryOfWarfare());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);     // 2 + 2
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Test
    @DisplayName("Creatures you control get +0/+2 during turns other than yours")
    void plusZeroTwoDuringOpponentTurn() {
        Permanent creature = addCreatureReady(player1, new QasaliPridemage());
        harness.addToBattlefield(player1, new GloryOfWarfare());

        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);     // 2 + 0
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4); // 2 + 2
    }

    @Test
    @DisplayName("Boost switches between +2/+0 and +0/+2 as the active player changes")
    void boostTogglesWithActivePlayer() {
        Permanent creature = addCreatureReady(player1, new QasaliPridemage());
        harness.addToBattlefield(player1, new GloryOfWarfare());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost the opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent enemy = addCreatureReady(player2, new QasaliPridemage());
        harness.addToBattlefield(player1, new GloryOfWarfare());

        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, enemy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enemy)).isEqualTo(2);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("Animated Glory of Warfare boosts itself during its controller's turn")
    void animatedGloryBoostsItsOwnPower() {
        Permanent glory = harness.addToBattlefieldAndReturn(player1, new GloryOfWarfare());
        harness.addToBattlefield(player1, new Opalescence());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, glory)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, glory)).isEqualTo(4);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("Animated Glory of Warfare boosts itself during another player's turn")
    void animatedGloryBoostsItsOwnToughness() {
        Permanent glory = harness.addToBattlefieldAndReturn(player1, new GloryOfWarfare());
        harness.addToBattlefield(player1, new Opalescence());
        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, glory)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, glory)).isEqualTo(6);
    }

    @Test
    @DisplayName("Multiple copies boost newly entering creatures and stop boosting when removed")
    void multipleCopiesAffectNewCreaturesAndStopWhenRemoved() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GloryOfWarfare());
        harness.addToBattlefield(player1, new GloryOfWarfare());
        Permanent creature = harness.enterBattlefieldAndReturn(player1, new QasaliPridemage());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);

        gd.playerBattlefields.get(player1.getId()).remove(first);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
