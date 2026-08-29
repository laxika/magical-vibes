package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.w.WallOfIce;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantOx.class, DuskLegionDreadnought.class, WallOfIce.class})
class GiantOxTest extends BaseCardTest {

    @Test
    void crewsUsingToughnessInsteadOfPower() {
        Permanent vehicle = addVehicleReady(player1);
        Permanent ox = addCreatureReady(player1, new GiantOx());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(ox.isTapped()).isTrue();
    }

    @Test
    void ordinaryCreaturesStillCrewUsingPower() {
        addVehicleReady(player1);
        addCreatureReady(player1, new WallOfIce());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power to crew");
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new DuskLegionDreadnought());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
