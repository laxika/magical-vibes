package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RingOfTheLucii.class, GrizzlyBears.class, Forest.class})
class RingOfTheLuciiTest extends BaseCardTest {

    @Test
    void tapsForTwoColorlessMana() {
        Permanent ring = addRing();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void paysLifeAndTapsTargetNonlandPermanent() {
        Permanent ring = addRing();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(ring.isTapped()).isTrue();
        assertThat(target.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void cannotTargetLand() {
        addRing();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a nonland permanent");
    }

    @Test
    void cannotPayLifeFromZero() {
        addRing();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLife(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addRing() {
        return harness.addToBattlefieldAndReturn(player1, new RingOfTheLucii());
    }
}
