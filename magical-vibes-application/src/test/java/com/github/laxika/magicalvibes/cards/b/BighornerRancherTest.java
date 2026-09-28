package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BighornerRancher.class, GrizzlyBears.class})
class BighornerRancherTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds green mana equal to the greatest creature power")
    void tappingAddsManaEqualToGreatestCreaturePower() {
        Permanent rancher = harness.addToBattlefieldAndReturn(player1, new BighornerRancher());
        rancher.setSummoningSick(false);
        GrizzlyBears other = new GrizzlyBears();
        other.setPower(4);
        harness.addToBattlefield(player1, other);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrificing gains life equal to the greatest other creature toughness")
    void sacrificingGainsLifeFromOtherCreatureToughness() {
        harness.addToBattlefield(player1, new BighornerRancher());
        GrizzlyBears other = new GrizzlyBears();
        other.setToughness(3);
        harness.addToBattlefield(player1, other);

        harness.setLife(player1, 10);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof BighornerRancher);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof BighornerRancher);
    }
}
