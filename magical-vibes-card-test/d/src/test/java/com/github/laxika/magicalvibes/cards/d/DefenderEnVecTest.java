package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.s.SealOfFire;
import com.github.laxika.magicalvibes.cards.t.TerrainGenerator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DefenderEnVec.class, SealOfFire.class, TerrainGenerator.class})
class DefenderEnVecTest extends BaseCardTest {

    @Test
    @DisplayName("Defender en-Vec enters with four fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new DefenderEnVec(), "{3}{W}");
        harness.passBothPriorities();

        Permanent defender = findPermanent(player1, "Defender en-Vec");
        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Defender en-Vec");
    }

    @Test
    @DisplayName("Fading sacrifices the creature when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new DefenderEnVec());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Defender en-Vec");
    }

    @Test
    @DisplayName("Removing a fade counter prevents the next 2 damage to any target")
    void removesCounterAndPreventsDamage() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        harness.addToBattlefield(player1, new SealOfFire());
        Permanent target = addCreatureReady(player2, new DefenderEnVec());

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(defender.getCounterCount(CounterType.FADE)).isZero();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing a fade counter prevents the next 2 damage to a target player")
    void preventsDamageToTargetPlayer() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        harness.addToBattlefield(player1, new SealOfFire());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("The prevention ability cannot be activated without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        addCreatureReady(player1, new DefenderEnVec());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The prevention ability cannot target a land")
    void cannotTargetLand() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TerrainGenerator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void removingLastCounterAtUpkeepDoesNotSacrificeUntilNextUpkeep() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Defender en-Vec");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Defender en-Vec");
        harness.assertInGraveyard(player1, "Defender en-Vec");
    }

    @Test
    void opponentUpkeepDoesNotRemoveFadeCounters() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(2);
    }

    @Test
    void preventionSurvivesSourceSacrificeDuringUpkeep() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        harness.addToBattlefield(player1, new SealOfFire());

        advanceToUpkeep(player1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Defender en-Vec");
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    void multipleActivationsStackButShieldIsConsumed() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 2);
        harness.addToBattlefield(player1, new SealOfFire());
        harness.addToBattlefield(player1, new SealOfFire());
        harness.addToBattlefield(player1, new SealOfFire());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Defender en-Vec");

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void tappedNewlyEnteredDefenderCanActivate() {
        Permanent defender = harness.enterBattlefieldAndReturn(player1, new DefenderEnVec());
        defender.tap();
        harness.addToBattlefield(player1, new SealOfFire());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(defender.getCounterCount(CounterType.FADE)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void unusedPreventionExpiresAtEndOfTurn() {
        Permanent defender = addCreatureReady(player1, new DefenderEnVec());
        defender.setCounterCount(CounterType.FADE, 1);
        harness.addToBattlefield(player2, new SealOfFire());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
}
