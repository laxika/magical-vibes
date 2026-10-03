package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({ChandraNalaar.class, WoodlandChangeling.class})
class ChandraNalaarTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts planeswalker spell on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ChandraNalaar()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castPlaneswalker(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.PLANESWALKER_SPELL);
    }

    @Test
    @DisplayName("Resolving puts planeswalker on battlefield with initial loyalty 6")
    void resolvingEntersBattlefieldWithLoyalty() {
        harness.setHand(player1, List.of(new ChandraNalaar()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chandra Nalaar");
        Permanent chandra = findPermanent(player1, "Chandra Nalaar");
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(chandra.isSummoningSick()).isFalse();
    }

    @Test
    @DisplayName("+1 ability deals 1 damage to target player and increases loyalty")
    void plusOneDeals1DamageToPlayer() {
        Permanent chandra = addReadyChandra(player1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(7); // 6 + 1
        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("-X ability deals X damage to target creature and removes X loyalty")
    void minusXDealsXDamageToCreature() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());

        Permanent bear = findPermanent(player2, "Woodland Changeling");

        // Use X=3 to deal 3 damage (kills 2/2 bear)
        harness.activateAbility(player1, 0, 1, 3, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3); // 6 - 3
        // Bear should be dead (3 damage to a 2/2)
        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("-X ability with X=0 deals 0 damage")
    void minusXWithZeroDealsZeroDamage() {
        Permanent chandra = addReadyChandra(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());

        Permanent bear = findPermanent(player2, "Woodland Changeling");

        harness.activateAbility(player1, 0, 1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6); // 6 - 0
        // Bear should still be alive (0 damage)
        harness.assertOnBattlefield(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("-X ability cannot use more loyalty than available")
    void minusXCannotExceedLoyalty() {
        addReadyChandra(player1);
        harness.addToBattlefield(player2, new WoodlandChangeling());

        Permanent bear = findPermanent(player2, "Woodland Changeling");

        // X=7 but Chandra only has 6 loyalty
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 7, bear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("-8 ability deals 10 damage to target player and their creatures")
    void minusEightDeals10DamageToPlayerAndCreatures() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 8);
        harness.addToBattlefield(player2, new WoodlandChangeling());
        harness.addToBattlefield(player2, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Chandra should have 0 loyalty (8 - 8) and be in graveyard
        harness.assertNotOnBattlefield(player1, "Chandra Nalaar");

        // Player 2 takes 10 damage
        int lifeAfter = gd.playerLifeTotals.get(player2.getId());
        assertThat(lifeAfter).isEqualTo(10); // 20 - 10

        // Both bears should be dead (10 damage to 2/2)
        harness.assertNotOnBattlefield(player2, "Woodland Changeling");
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(c -> c.getName().equals("Woodland Changeling"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 ability does not damage controller's own creatures")
    void minusEightDoesNotDamageOwnCreatures() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 8);
        harness.addToBattlefield(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 2, null, player2.getId());
        harness.passBothPriorities();

        // Player 1's bear should be unharmed
        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    @DisplayName("Cannot use -8 when loyalty is only 6")
    void cannotActivateMinusEightWithInsufficientLoyalty() {
        Permanent chandra = addReadyChandra(player1);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate loyalty ability during opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        addReadyChandra(player1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyChandra(player1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    void plusOneDamagesPlaneswalkerWithoutDamagingItsController() {
        addReadyChandra(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    void playerDamageAbilitiesCannotTargetCreatures() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 9);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(9);
    }

    @Test
    void minusXCannotTargetPlayerOrNoncreaturePlaneswalker() {
        Permanent chandra = addReadyChandra(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    void minusXStillResolvesAfterSpendingAllLoyalty() {
        addReadyChandra(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 1, 6, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chandra Nalaar");
        harness.assertInGraveyard(player2, "Woodland Changeling");
    }

    @Test
    void minusEightDamagesPlaneswalkerAndItsControllersCreaturesOnly() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 9);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 12);
        harness.addToBattlefield(player2, new WoodlandChangeling());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Woodland Changeling");
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Woodland Changeling");
    }

    @Test
    void minusEightDoesNothingWhenItsOnlyTargetLeavesBattlefield() {
        Permanent chandra = addReadyChandra(player1);
        chandra.setCounterCount(CounterType.LOYALTY, 9);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        target.setCounterCount(CounterType.LOYALTY, 6);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new WoodlandChangeling());

        harness.activateAbility(player1, 0, 2, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Woodland Changeling");
        harness.assertLife(player2, 20);
    }

    private Permanent addReadyChandra(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ChandraNalaar());
        perm.setCounterCount(CounterType.LOYALTY, 6);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
