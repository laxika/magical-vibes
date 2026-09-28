package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VATS.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class VATSTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys any number of target creatures with equal toughness")
    void destroysEqualToughnessTargets() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(List.of(ownBear.getId(), opposingBear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(hillGiant);
    }

    @Test
    @DisplayName("Rejects targets with different toughness")
    void rejectsDifferentToughnessTargets() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        prepareCard();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bear.getId(), hillGiant.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("equal toughness");
    }

    @Test
    @DisplayName("Split second prevents casting spells in response")
    void splitSecondPreventsResponses() {
        prepareCard();
        harness.castInstant(player1, 0, List.of());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<java.util.UUID> targetIds) {
        prepareCard();
        harness.castInstant(player1, 0, targetIds);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new VATS()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
