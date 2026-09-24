package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiligreeAttendant.class, DarksteelRelic.class, GrizzlyBears.class})
class FiligreeAttendantTest extends BaseCardTest {

    @Test
    void powerEqualsArtifactsItsControllerControlsAndToughnessStaysThree() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);

        harness.addToBattlefield(player1, new DarksteelRelic());
        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }

    @Test
    void countsOnlyArtifactsControlledByItsController() {
        Permanent attendant = harness.addToBattlefieldAndReturn(player1, new FiligreeAttendant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new DarksteelRelic());

        assertThat(gqs.getEffectivePower(gd, attendant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attendant)).isEqualTo(3);
    }
}
