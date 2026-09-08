package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DynamiteDiverTest extends BaseCardTest {

    @Test
    @DisplayName("Its power bonus lets it crew a Vehicle with crew 3")
    void powerBonusLetsItCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FellFlagship());
        Permanent diver = harness.addToBattlefieldAndReturn(player1, new DynamiteDiver());
        diver.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(vehicle.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(diver.isTapped()).isTrue();
    }

    @Test
    @DisplayName("When it dies, it deals 1 damage to a chosen player")
    void dealsDamageWhenItDies() {
        Permanent diver = addCreatureReady(player1, new DynamiteDiver());
        TestCards.mutableCard(diver).setToughness(0);
        harness.setLife(player2, 20);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
