package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TolarianSentinel.class, AshcoatBear.class, Island.class})
class TolarianSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {U}, tapping, and discarding a card returns a permanent you control to its owner's hand")
    void returnsOwnPermanentToHand() {
        addCreatureReady(player1, new TolarianSentinel());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player1, "Island"));
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Tolarian Sentinel");
    }

    @Test
    @DisplayName("Activation pays blue mana, taps Tolarian Sentinel, and discards the chosen card")
    void paysActivationCost() {
        Permanent sentinel = addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(sentinel.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Cannot activate without blue mana")
    void cannotActivateWithoutBlueMana() {
        Permanent sentinel = addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(sentinel.isTapped()).isFalse();
        harness.assertInHand(player1, "Ashcoat Bear");
        harness.assertOnBattlefield(player1, "Island");
    }

    @Test
    @DisplayName("Cannot activate while Tolarian Sentinel is tapped")
    void cannotActivateWhenTapped() {
        Permanent sentinel = addCreatureReady(player1, new TolarianSentinel());
        sentinel.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        harness.assertInHand(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Cannot activate while Tolarian Sentinel has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new TolarianSentinel());
        sentinel.setSummoningSick(true);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");

        assertThat(sentinel.isTapped()).isFalse();
        harness.assertInHand(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Can return Tolarian Sentinel itself to its owner's hand")
    void canReturnItselfToHand() {
        Permanent sentinel = addCreatureReady(player1, new TolarianSentinel());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, sentinel.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tolarian Sentinel");
        harness.assertNotOnBattlefield(player1, "Tolarian Sentinel");
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Cannot target a permanent an opponent controls")
    void cannotTargetOpponentsPermanent() {
        addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability fizzles if the target is no longer controlled by its controller")
    void fizzlesIfTargetChangesController() {
        addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInHand(player1, "Island");
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("A permanent controlled by you but owned by an opponent returns to its owner's hand")
    void returnsBorrowedPermanentToOwnersHand() {
        addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.setHand(player1, List.of(new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ashcoat Bear");
        harness.assertNotInHand(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("An activated ability still resolves after Tolarian Sentinel leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent firstSentinel = addCreatureReady(player1, new TolarianSentinel());
        addCreatureReady(player1, new TolarianSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new AshcoatBear(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 1, null, firstSentinel.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Tolarian Sentinel");
        harness.assertOnBattlefield(player1, "Island");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

}
