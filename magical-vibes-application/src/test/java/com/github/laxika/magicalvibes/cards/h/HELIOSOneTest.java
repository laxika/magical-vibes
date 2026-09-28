package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HELIOSOne.class, GrizzlyBears.class, HillGiant.class})
class HELIOSOneTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new HELIOSOne());

        harness.activateAbility(player1, 0, 0, null, null);

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void tapsToCreateAnEnergyCounter() {
        harness.addToBattlefield(player1, new HELIOSOne());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysXEnergyAndSacrificesToDestroyAPermanentWithManaValueX() {
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, 2, target.getId());

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        harness.assertInGraveyard(player1, "HELIOS One");
        harness.assertOnBattlefield(player2, "Grizzly Bears");

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetAPermanentWithManaValueAboveX() {
        harness.addToBattlefield(player1, new HELIOSOne());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        gd.playerEnergyCounters.put(player1.getId(), 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        harness.assertOnBattlefield(player1, "HELIOS One");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }
}
