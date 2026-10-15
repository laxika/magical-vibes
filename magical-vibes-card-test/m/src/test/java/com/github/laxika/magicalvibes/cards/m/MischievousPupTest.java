package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MischievousPup.class, Forest.class})
class MischievousPupTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another permanent you control to its owner's hand")
    void etbReturnsAnotherPermanentYouControl() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castPup(List.of(forest.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    @Test
    @DisplayName("ETB can choose no permanent")
    void etbCanChooseNoPermanent() {
        harness.castFromHand(player1, new MischievousPup(), "{2}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castPup(List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent you control");
    }

    @Test
    void canChooseNoTargetEvenWhenAnotherPermanentIsAvailable() {
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new MischievousPup(), "{2}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mischievous Pup");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void canReturnAnotherMischievousPup() {
        Permanent otherPup = harness.addToBattlefieldAndReturn(player1, new MischievousPup());
        castPup(List.of(otherPup.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherPup);
        harness.assertInHand(player1, "Mischievous Pup");
        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    @Test
    void returnsBorrowedPermanentToItsOwnersHand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(forest.getId(), player2.getId());
        castPup(List.of(forest.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    void targetThatChangesControllerIsNotReturned() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castPup(List.of(forest.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        gd.stolenCreatures.put(forest.getId(), player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotInHand(player2, "Forest");
    }

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new MischievousPup(), "{2}{W}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mischievous Pup");
    }

    private void castPup(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MischievousPup()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
