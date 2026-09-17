package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlessedOrator.class, GrizzlyBears.class, Plains.class})
class BlessedOratorTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control get +0/+1")
    void buffsOtherOwnCreatures() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new BlessedOrator());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent orator = harness.addToBattlefieldAndReturn(player1, new BlessedOrator());

        assertThat(gqs.getEffectivePower(gd, orator)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, orator)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff opponent's creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new BlessedOrator());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Blessed Orators give +0/+2 to other creatures")
    void twoOratorsStackBonuses() {
        harness.addToBattlefield(player1, new BlessedOrator());
        harness.addToBattlefield(player1, new BlessedOrator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff own noncreature permanents")
    void doesNotBuffNoncreaturePermanents() {
        harness.addToBattlefield(player1, new BlessedOrator());
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, plains)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, plains)).isZero();
    }

    @Test
    @DisplayName("Bonus is removed when Blessed Orator leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BlessedOrator());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Blessed Orator"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }
}
