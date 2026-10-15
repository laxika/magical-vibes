package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AdventurersInn.class)
class AdventurersInnTest extends BaseCardTest {

    @Test
    void entersAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new AdventurersInn()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    void tapsForColorlessMana() {
        Permanent inn = harness.addToBattlefieldAndReturn(player1, new AdventurersInn());
        inn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(inn.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void canTapImmediatelyWhileLifeGainTriggerIsPending() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new AdventurersInn()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent inn = findPermanent(player1, "Adventurer's Inn");
        assertThat(inn.isTapped()).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(inn.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
