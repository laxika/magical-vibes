package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FelidarSovereign.class})
class FelidarSovereignTest extends BaseCardTest {

    @Test
    @DisplayName("Wins the game at upkeep with exactly 40 life")
    void winsWithExactlyFortyLife() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 40);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep with less than 40 life")
    void doesNotTriggerBelowFortyLife() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 39);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 40);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win if life drops below 40 before resolution")
    void conditionIsCheckedAgainOnResolution() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 40);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setLife(player1, 39);
        harness.passBothPriorities();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void winsWithMoreThanFortyLife() {
        harness.addToBattlefield(player2, new FelidarSovereign());
        harness.setLife(player2, 41);
        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player2.getId());
    }

    @Test
    void winsIfLifeReturnsToFortyBeforeResolution() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 40);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.setLife(player1, 39);
        harness.setLife(player1, 40);
        harness.passBothPriorities();
        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.winnerPlayerId).isEqualTo(player1.getId());
    }

    @Test
    void gainingLifeAfterUpkeepBeginsDoesNotCreateTrigger() {
        harness.addToBattlefield(player1, new FelidarSovereign());
        harness.setLife(player1, 39);
        advanceToUpkeep(player1);
        harness.setLife(player1, 40);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    void attacksWithoutTappingAndGainsLifeFromCombatDamage() {
        var sovereign = harness.addToBattlefieldAndReturn(player1, new FelidarSovereign());
        sovereign.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveCombat();
        assertThat(sovereign.isTapped()).isFalse();
        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }

}
