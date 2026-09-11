package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenMauler.class, GrizzlyBears.class, LeoninScimitar.class})
class DwarvenMaulerTest extends BaseCardTest {

    @Test
    @DisplayName("Equip abilities targeting Dwarven Mauler cost {2} less")
    void equipmentAbilitiesTargetingDwarvenMaulerCostTwoLess() {
        Permanent mauler = harness.addToBattlefieldAndReturn(player1, new DwarvenMauler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null,
                mauler.getId());
        harness.passBothPriorities();

        assertThat(scimitar.getAttachedTo()).isEqualTo(mauler.getId());
    }

    @Test
    @DisplayName("Equip abilities targeting another creature are not reduced")
    void equipmentAbilitiesTargetingAnotherCreatureAreNotReduced() {
        harness.addToBattlefield(player1, new DwarvenMauler());
        Permanent scimitar = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(scimitar), null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
