package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.cards.s.SeraphicSteed;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotshotMechanic.class, FellFlagship.class, SeraphicSteed.class})
class HotshotMechanicTest extends BaseCardTest {

    @Test
    @DisplayName("Its power bonus lets it crew a Vehicle")
    void powerBonusLetsItCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FellFlagship());
        Permanent mechanic = addCreatureReady(player1, new HotshotMechanic());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(mechanic.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The crew power bonus does not help pay saddle costs")
    void crewBonusDoesNotApplyToSaddle() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        Permanent mechanic = addCreatureReady(player1, new HotshotMechanic());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to saddle");

        assertThat(steed.isSaddled()).isFalse();
        assertThat(mechanic.isTapped()).isFalse();
    }
}
