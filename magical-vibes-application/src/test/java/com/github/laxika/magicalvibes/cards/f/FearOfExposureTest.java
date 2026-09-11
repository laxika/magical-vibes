package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FearOfExposure.class, Forest.class, GrizzlyBears.class})
class FearOfExposureTest extends BaseCardTest {

    @Test
    @DisplayName("Taps a creature and a land as an additional cost")
    void tapsCreatureAndLand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureTappingPermanents(player1, 0, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(land.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fear of Exposure");
    }

    @Test
    @DisplayName("Requires exactly two creatures and/or lands")
    void rejectsFewerThanTwoEligiblePermanents() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FearOfExposure()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreatureTappingPermanents(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(creature.isTapped()).isFalse();
        harness.assertInHand(player1, "Fear of Exposure");
    }
}
