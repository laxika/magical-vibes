package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AetherRefinery.class})
class AetherRefineryTest extends BaseCardTest {

    @Test
    void doublesEnergyThenCreatesTokenFromPaidEnergy() {
        harness.addToBattlefield(player1, new AetherRefinery());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.XValueChoice.class);

        harness.handleXValueChosen(player1, 2);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void mayDeclineEnergyPayment() {
        harness.addToBattlefield(player1, new AetherRefinery());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void canPayPreviouslyStoredEnergyAlongWithNewEnergy() {
        harness.addToBattlefield(player1, new AetherRefinery());
        gd.setPlayerEnergyCounters(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(5);
        harness.handleXValueChosen(player1, 5);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(5);
                    assertThat(token.getEffectiveToughness()).isEqualTo(5);
                });
    }

    @Test
    void canPayOnlyOneEnergyAndKeepTheRest() {
        harness.addToBattlefield(player1, new AetherRefinery());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 1);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(1);
                    assertThat(token.getEffectiveToughness()).isEqualTo(1);
                });
    }

    @Test
    void multipleRefineriesMultiplyEnergyWithoutUsingOpponentsRefinery() {
        harness.addToBattlefield(player1, new AetherRefinery());
        harness.addToBattlefield(player1, new AetherRefinery());
        harness.addToBattlefield(player2, new AetherRefinery());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        harness.handleXValueChosen(player1, 4);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .singleElement().satisfies(token -> {
                    assertThat(token.getEffectivePower()).isEqualTo(4);
                    assertThat(token.getEffectiveToughness()).isEqualTo(4);
                });
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
