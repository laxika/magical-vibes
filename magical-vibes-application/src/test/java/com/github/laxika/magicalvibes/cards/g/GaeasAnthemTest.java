package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GaeasAnthem.class, GrizzlyBears.class, GiantSpider.class})
class GaeasAnthemTest extends BaseCardTest {

    @Test
    void boostsAllCreaturesControllerOwns() {
        harness.addToBattlefield(player1, new GaeasAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GiantSpider());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent spider = findPermanent(player1, "Giant Spider");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(5);
    }

    @Test
    void doesNotBoostOpponentsCreatures() {
        harness.addToBattlefield(player1, new GaeasAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    void bonusIsRemovedWhenAnthemLeavesBattlefield() {
        harness.addToBattlefield(player1, new GaeasAnthem());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard() instanceof GaeasAnthem);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
