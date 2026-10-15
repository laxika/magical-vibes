package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShiarSoldier.class, SavageLands.class})
class ShiarSoldierTest extends BaseCardTest {

    @Test
    @DisplayName("Returns another permanent you control to its owner's hand")
    void returnsAnotherPermanentYouControl() {
        Permanent soldier = addCreatureReady(player1, new ShiarSoldier());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, soldier.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Savage Lands");
        harness.assertNotOnBattlefield(player1, "Savage Lands");
        harness.assertOnBattlefield(player1, "Shi'ar Soldier");
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentPermanent() {
        addCreatureReady(player1, new ShiarSoldier());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new SavageLands());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated during its controller's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new ShiarSoldier());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canActivateDuringOwnUpkeepAndPaysTapCost() {
        Permanent soldier = addCreatureReady(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(soldier.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Shi'ar Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(soldier).doesNotContain(target);
    }

    @Test
    void cannotActivateWithoutBlueMana() {
        Permanent soldier = addCreatureReady(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(soldier.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent soldier = addCreatureReady(player1, new ShiarSoldier());
        soldier.tap();
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsBorrowedPermanentToItsOwner() {
        addCreatureReady(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shi'ar Soldier");
        harness.assertNotInHand(player1, "Shi'ar Soldier");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void doesNotReturnTargetThatOpponentNowControls() {
        addCreatureReady(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ShiarSoldier());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        harness.assertNotInHand(player1, "Shi'ar Soldier");
        harness.assertNotInHand(player2, "Shi'ar Soldier");
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent soldier = addCreatureReady(player1, new ShiarSoldier());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SavageLands());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(soldier);
        gd.playerGraveyards.get(player1.getId()).add(soldier.getCard());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Savage Lands");
        harness.assertNotOnBattlefield(player1, "Savage Lands");
        harness.assertInGraveyard(player1, "Shi'ar Soldier");
    }
}
