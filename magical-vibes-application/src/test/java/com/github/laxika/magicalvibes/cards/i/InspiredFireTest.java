package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InspiredFire.class, GrizzlyBears.class, Mountain.class})
class InspiredFireTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one red mana after drawing three cards and deals four damage")
    void reducesCostByCardsDrawnAndDealsDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InspiredFire()));
        gd.cardsDrawnThisTurn.put(player1.getId(), 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Requires the full generic cost when no cards were drawn")
    void requiresFullCostWithoutDraws() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InspiredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new InspiredFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
