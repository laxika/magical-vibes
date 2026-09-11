package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponsTrainer.class, GrizzlyBears.class, LeoninScimitar.class})
class WeaponsTrainerTest extends BaseCardTest {

    @Test
    void doesNotBoostOtherCreaturesWithoutEquipment() {
        harness.addToBattlefield(player1, new WeaponsTrainer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostsOtherCreaturesWhenControllerHasEquipment() {
        harness.addToBattlefield(player1, new WeaponsTrainer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void opponentEquipmentDoesNotEnableBonus() {
        harness.addToBattlefield(player1, new WeaponsTrainer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void doesNotBoostWeaponsTrainerItself() {
        Permanent trainer = harness.addToBattlefieldAndReturn(player1, new WeaponsTrainer());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, trainer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, trainer)).isEqualTo(2);
    }

    @Test
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new WeaponsTrainer());
        harness.addToBattlefield(player1, new LeoninScimitar());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    void losesBonusWhenEquipmentLeavesBattlefield() {
        harness.addToBattlefield(player1, new WeaponsTrainer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Leonin Scimitar"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }
}
