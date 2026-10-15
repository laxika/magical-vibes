package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed({FearOfIsolation.class, GrizzlyBears.class, Island.class})
class FearOfIsolationTest extends BaseCardTest {

    @Test
    @DisplayName("A land can pay the return cost, and it returns before the spell resolves")
    void returnsLandBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new FearOfIsolation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, land.getId());

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Fear of Isolation");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fear of Isolation");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("A controlled permanent owned by an opponent returns to that opponent's hand")
    void returnsBorrowedPermanentToOwner() {
        Island borrowedLand = new Island();
        borrowedLand.setOwnerId(player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, borrowedLand);
        harness.setHand(player1, List.of(new FearOfIsolation()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, land.getId());

        harness.assertInHand(player2, "Island");
        harness.assertNotInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fear of Isolation");
    }

    @Test
    @DisplayName("Returns a permanent you control as an additional cost and enters the battlefield")
    void returnsControlledPermanentAndEntersBattlefield() {
        Permanent returnedPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FearOfIsolation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorceryWithSacrifice(player1, 0, returnedPermanent.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Fear of Isolation");
    }

    @Test
    @DisplayName("Cannot cast without returning a permanent")
    void cannotCastWithoutReturningPermanent() {
        harness.setHand(player1, List.of(new FearOfIsolation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return an opponent's permanent as the additional cost")
    void cannotReturnOpponentsPermanent() {
        Permanent opponentPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FearOfIsolation()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
