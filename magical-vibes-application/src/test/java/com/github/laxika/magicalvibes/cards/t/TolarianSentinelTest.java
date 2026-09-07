package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianSentinel.class, GrizzlyBears.class, Island.class})
class TolarianSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {U}, tapping, and discarding a card returns a permanent you control to its owner's hand")
    void returnsOwnPermanentToHand() {
        addReadySentinel();
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Island"));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Tolarian Sentinel");
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentsPermanent() {
        addReadySentinel();
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addReadySentinel();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addReadySentinel() {
        Permanent sentinel = new Permanent(new TolarianSentinel());
        sentinel.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentinel);
    }
}
