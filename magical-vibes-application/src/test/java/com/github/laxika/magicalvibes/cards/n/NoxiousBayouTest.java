package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NoxiousBayou.class)
class NoxiousBayouTest extends BaseCardTest {

    @Test
    void tapsForBlackManaAndGivesItsControllerPoison() {
        Permanent bayou = harness.addToBattlefieldAndReturn(player1, new NoxiousBayou());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tapsForGreenManaAndGivesItsControllerPoison() {
        Permanent bayou = harness.addToBattlefieldAndReturn(player1, new NoxiousBayou());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(1);
        assertThat(bayou.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void eachActivationAddsOnePoisonCounterToExistingCounters() {
        harness.addToBattlefield(player1, new NoxiousBayou());
        harness.addToBattlefield(player1, new NoxiousBayou());
        gd.playerPoisonCounters.put(player1.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opposingControllerReceivesTheManaAndPoison() {
        Permanent bayou = harness.addToBattlefieldAndReturn(player2, new NoxiousBayou());

        harness.activateAbility(player2, 0, null, null);
        harness.handleListChoice(player2, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(bayou.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
