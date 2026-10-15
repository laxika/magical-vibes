package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.cards.w.WallOfIce;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantOx.class, DuskLegionDreadnought.class, WallOfIce.class, TrainedArynx.class})
class GiantOxTest extends BaseCardTest {

    @Test
    void crewsUsingToughnessInsteadOfPower() {
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        Permanent ox = addCreatureReady(player1, new GiantOx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(ox.isTapped()).isTrue();
    }

    @Test
    void ordinaryCreaturesStillCrewUsingPower() {
        addCreatureReady(player1, new DuskLegionDreadnought());
        addCreatureReady(player1, new WallOfIce());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    @Test
    void usesModifiedToughnessEvenWhenPowerWouldBeEnough() {
        addCreatureReady(player1, new DuskLegionDreadnought());
        Permanent ox = addCreatureReady(player1, new GiantOx());
        ox.setToughnessModifier(-5);
        ox.setPowerModifier(5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
        assertThat(ox.isTapped()).isFalse();
    }

    @Test
    void canCrewWhileSummoningSick() {
        Permanent vehicle = addCreatureReady(player1, new DuskLegionDreadnought());
        Permanent ox = harness.addToBattlefieldAndReturn(player1, new GiantOx());
        ox.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(ox.isTapped()).isTrue();
    }

    @Test
    void cannotUseToughnessToSaddleAMount() {
        Permanent mount = addCreatureReady(player1, new TrainedArynx());
        Permanent ox = addCreatureReady(player1, new GiantOx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to saddle");
        assertThat(ox.isTapped()).isFalse();
        assertThat(mount.isSaddled()).isFalse();
    }
}
