package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BenalishLancer;
import com.github.laxika.magicalvibes.cards.s.StormscapeApprentice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JuntuStakes.class, StormscapeApprentice.class, BenalishLancer.class})
class JuntuStakesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature with power 1 or less does not untap while Juntu Stakes is out")
    void power1CreatureStaysTapped() {
        harness.addToBattlefield(player1, new JuntuStakes());
        Permanent creature = addCreatureReady(player1, new StormscapeApprentice());
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature with power greater than 1 untaps normally")
    void power2CreatureUntaps() {
        harness.addToBattlefield(player1, new JuntuStakes());
        Permanent creature = addCreatureReady(player1, new BenalishLancer());
        creature.tap();

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' creatures during their untap step")
    void affectsOpponentCreatures() {
        harness.addToBattlefield(player1, new JuntuStakes());
        Permanent opponentCreature = addCreatureReady(player2, new StormscapeApprentice());
        opponentCreature.tap();

        advanceToUpkeep(player2);

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Juntu Stakes leaves, low-power creatures untap again")
    void untapsAfterStakesLeaves() {
        Permanent stakes = harness.addToBattlefieldAndReturn(player1, new JuntuStakes());
        Permanent creature = addCreatureReady(player1, new StormscapeApprentice());
        creature.tap();

        gd.playerBattlefields.get(player1.getId()).remove(stakes);

        advanceToUpkeep(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Noncreature permanents are not affected")
    void noncreaturePermanentUntaps() {
        Permanent stakes = harness.addToBattlefieldAndReturn(player1, new JuntuStakes());
        stakes.tap();

        advanceToUpkeep(player1);

        assertThat(stakes.isTapped()).isFalse();
    }
}
