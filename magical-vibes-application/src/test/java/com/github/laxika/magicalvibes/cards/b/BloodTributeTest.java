package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VampireNeonate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodTributeTest extends BaseCardTest {

    @Test
    void targetOpponentLosesHalfLifeRoundedUp() {
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 20);
    }

    @Test
    void kickerTapsVampireAndControllerGainsLifeLost() {
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireNeonate());
        harness.setLife(player2, 21);
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castKickedSorceryWithTap(player1, 0, player2.getId(), vampire.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 10);
        harness.assertLife(player1, 31);
        assertThat(vampire.isTapped()).isTrue();
    }

    @Test
    void cannotTargetController() {
        harness.setHand(player1, List.of(new BloodTribute()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }
}
