package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RimescaleDragon.class, SnowCoveredForest.class, BorealDruid.class})
class RimescaleDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target creature and puts an ice counter on it")
    void tapsTargetCreatureAndPutsIceCounterOnIt() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature with an ice counter does not untap during its controller's untap step")
    void creatureWithIceCounterDoesNotUntap() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        target.tap();
        target.setCounterCount(CounterType.ICE, 1);

        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only creatures with ice counters are prevented from untapping")
    void onlyCreaturesWithIceCountersArePreventedFromUntapping() {
        addDragon(player1);
        Permanent creatureWithoutIceCounter = addCreature(player2);
        creatureWithoutIceCounter.tap();
        Permanent landWithIceCounter = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        landWithIceCounter.tap();
        landWithIceCounter.setCounterCount(CounterType.ICE, 1);

        advanceToUpkeep(player2);

        assertThat(creatureWithoutIceCounter.isTapped()).isFalse();
        assertThat(landWithIceCounter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addDragon(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SnowCoveredForest());
        payAbilityCost(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Regular mana cannot pay the snow activation cost")
    void regularManaCannotPaySnowCost() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void repeatedActivationAddsCountersToAnAlreadyTappedCreature() {
        Permanent dragon = addDragon(player1);
        dragon.tap();
        Permanent target = addCreature(player2);
        target.tap();
        target.setCounterCount(CounterType.ICE, 1);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(2);
    }

    @Test
    void summoningSickDragonCanActivateUsingManaFromSnowLand() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new RimescaleDragon());
        dragon.setSummoningSick(true);
        harness.addToBattlefield(player1, new SnowCoveredForest());
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.tapPermanent(player1, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(dragon.isTapped()).isFalse();
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    void canTargetItselfAndPreventsItsOwnUntap() {
        Permanent dragon = addDragon(player1);
        payAbilityCost(player1);

        harness.activateAbility(player1, 0, null, dragon.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);

        assertThat(dragon.isTapped()).isTrue();
        assertThat(dragon.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    void removingAllIceCountersAllowsUntapping() {
        addDragon(player1);
        Permanent target = addCreature(player2);
        target.tap();
        target.setCounterCount(CounterType.ICE, 1);
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();

        target.setCounterCount(CounterType.ICE, 0);
        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void leavingTheBattlefieldEndsTheUntapRestrictionButKeepsCounters() {
        Permanent dragon = addDragon(player1);
        Permanent target = addCreatureReady(player2, new BorealDruid());
        payAbilityCost(player1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerGraveyards.get(player1.getId()).add(dragon.getCard());
        advanceToUpkeep(player2);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
    }

    @Test
    void abilityResolvesEvenIfDragonLeavesBeforeResolution() {
        Permanent dragon = addDragon(player1);
        Permanent target = addCreatureReady(player2, new BorealDruid());
        payAbilityCost(player1);
        harness.activateAbility(player1, 0, null, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(dragon);
        gd.playerGraveyards.get(player1.getId()).add(dragon.getCard());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.ICE)).isEqualTo(1);
        advanceToUpkeep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    private Permanent addDragon(Player player) {
        return addCreatureReady(player, new RimescaleDragon());
    }

    private Permanent addCreature(Player player) {
        return addCreatureReady(player, new RimescaleDragon());
    }

    private void payAbilityCost(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        gd.playerManaPools.get(player.getId()).addSnowMana(ManaColor.COLORLESS, 1);
    }

}
