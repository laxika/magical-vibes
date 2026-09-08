package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShiftingBordersTest extends BaseCardTest {

    @Test
    @DisplayName("Exchanges control of the two target lands")
    void exchangesControlOfLands() {
        harness.setHand(player1, List.of(new ShiftingBorders()));
        addManaForShiftingBorders();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.castAndResolveInstant(player1, 0, List.of(own.getId(), opponent.getId()));

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player2, "Island");
    }

    @Test
    @DisplayName("Cannot target your own land as the opponent's land")
    void cannotTargetOwnLandAsOpponentTarget() {
        harness.setHand(player1, List.of(new ShiftingBorders()));
        addManaForShiftingBorders();
        Permanent own = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent alsoOwn = harness.addToBattlefieldAndReturn(player1, new Island());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(own.getId(), alsoOwn.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land an opponent controls");
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and exchanges the additional target lands")
    void splicesOntoArcaneSpell() {
        Permanent ownFirst = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentFirst = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent ownSecond = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opponentSecond = harness.addToBattlefieldAndReturn(player2, new Mountain());
        ShiftingBorders spliced = new ShiftingBorders();

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ShiftingBorders(), spliced));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        gs.playCardWithSplice(gd, player1, 0, 0, null, null,
                List.of(ownFirst.getId(), opponentFirst.getId(), ownSecond.getId(), opponentSecond.getId()),
                List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
        harness.assertOnBattlefield(player2, "Forest");
        harness.assertOnBattlefield(player2, "Plains");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Mountain");
    }

    private void addManaForShiftingBorders() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
