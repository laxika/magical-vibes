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
}
