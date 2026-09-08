package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EarlyFrostTest extends BaseCardTest {

    @Test
    @DisplayName("Taps three target lands")
    void tapsThreeTargetLands() {
        Permanent land1 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent land2 = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent land3 = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new EarlyFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(land1.getId(), land2.getId(), land3.getId()));
        harness.passBothPriorities();

        assertThat(land1.isTapped()).isTrue();
        assertThat(land2.isTapped()).isTrue();
        assertThat(land3.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can target fewer than three lands")
    void canTargetFewerThanThreeLands() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new EarlyFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a nonland permanent")
    void cannotTargetNonlandPermanent() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EarlyFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land");
    }

    @Test
    @DisplayName("Cannot target more than three lands")
    void cannotTargetMoreThanThreeLands() {
        Permanent land1 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent land2 = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent land3 = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent land4 = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new EarlyFrost()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(land1.getId(), land2.getId(), land3.getId(), land4.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between");
    }
}
