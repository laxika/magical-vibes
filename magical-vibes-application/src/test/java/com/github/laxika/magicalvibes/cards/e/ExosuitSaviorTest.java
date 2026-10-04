package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExosuitSavior.class, Forest.class})
class ExosuitSaviorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returns another permanent you control to its owner's hand")
    void etbReturnsAnotherPermanentYouControl() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castExosuitSavior(List.of(forest.getId()));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Exosuit Savior");
    }

    @Test
    @DisplayName("ETB can choose no permanent")
    void etbCanChooseNoPermanent() {
        castExosuitSavior(List.of());

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Exosuit Savior");
    }

    @Test
    @DisplayName("Cannot target an opponent's permanent")
    void cannotTargetOpponentsPermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> castExosuitSavior(List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another permanent you control");
    }

    @Test
    @DisplayName("ETB may decline to return a permanent even when a legal target exists")
    void canDeclineWithLegalTarget() {
        harness.addToBattlefield(player1, new Forest());
        castExosuitSavior(List.of());

        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Exosuit Savior");
    }

    @Test
    @DisplayName("ETB can return a different Exosuit Savior")
    void canReturnAnotherSavior() {
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ExosuitSavior());
        castExosuitSavior(List.of(other.getId()));

        resolveAllTriggers();

        harness.assertInHand(player1, "Exosuit Savior");
        harness.assertOnBattlefield(player1, "Exosuit Savior");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(other.getId()));
    }

    @Test
    @DisplayName("A borrowed permanent returns to its owner rather than its controller")
    void returnsBorrowedPermanentToOwner() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        gd.stolenCreatures.put(forest.getId(), player2.getId());
        castExosuitSavior(List.of(forest.getId()));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("ETB does not return a target that an opponent controls at resolution")
    void targetChangingControllerIsIllegal() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        castExosuitSavior(List.of(forest.getId()));
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        gd.stolenCreatures.put(forest.getId(), player1.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertNotInHand(player1, "Forest");
        harness.assertNotInHand(player2, "Forest");
        harness.assertOnBattlefield(player1, "Exosuit Savior");
    }

    private void castExosuitSavior(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new ExosuitSavior()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
