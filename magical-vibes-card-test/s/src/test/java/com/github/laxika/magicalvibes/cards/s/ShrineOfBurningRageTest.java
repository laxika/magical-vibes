package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GutShot;
import com.github.laxika.magicalvibes.cards.k.KarnLiberated;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShrineOfBurningRage.class, GutShot.class, SuturePriest.class, KarnLiberated.class})
class ShrineOfBurningRageTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger puts a charge counter on Shrine")
    void upkeepTriggerAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // upkeep trigger goes on stack
        harness.passBothPriorities(); // resolve PutCountersOnSelfEffect

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent shrine = addReadyShrine(player1);

        // First upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Second upkeep
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a red spell puts a charge counter on Shrine")
    void castingRedSpellAddsChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve charge counter trigger
        harness.passBothPriorities(); // resolve Gut Shot

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-red spell does not put a charge counter on Shrine")
    void castingNonRedSpellDoesNotAddChargeCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player1, List.of(new SuturePriest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Sacrificing Shrine deals damage equal to charge counters to target player")
    void sacrificeDealsDamageToPlayer() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Sacrificing Shrine with 0 counters deals 0 damage")
    void sacrificeWithZeroCountersDealZeroDamage() {
        addReadyShrine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Sacrificing Shrine deals damage to target creature")
    void sacrificeDealsDamageToCreature() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new SuturePriest()).getId();

        harness.activateAbility(player1, 0, null, creatureId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Suture Priest");
        harness.assertInGraveyard(player2, "Suture Priest");
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Charge counters are snapshotted before sacrifice so damage is correct")
    void chargeCountersSnapshotBeforeSacrifice() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 7);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        // Shrine is sacrificed immediately as cost
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");

        harness.passBothPriorities();

        // Damage should still equal 7 even though Shrine is gone
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("Activated ability requires tap — cannot activate when tapped")
    void activatedAbilityRequiresTap() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        shrine.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent's upkeep does not add a charge counter")
    void opponentsUpkeepDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Opponent casting a red spell does not add a charge counter")
    void opponentsRedSpellDoesNotAddCounter() {
        Permanent shrine = addReadyShrine(player1);
        harness.setHand(player2, List.of(new GutShot()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(shrine.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Sacrificing in response to a counter trigger uses only counters already on Shrine")
    void pendingCounterTriggerDoesNotIncreaseSacrificeDamage() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 2);
        harness.setHand(player1, List.of(new GutShot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        harness.assertNotOnBattlefield(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Activated ability requires three mana and does not sacrifice Shrine when payment fails")
    void insufficientManaDoesNotSacrificeShrine() {
        addReadyShrine(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, player2.getId())
        ).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Shrine of Burning Rage");
        harness.assertNotInGraveyard(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("A newly entered noncreature Shrine may activate its tap ability")
    void newlyEnteredShrineCanActivate() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new ShrineOfBurningRage());
        shrine.setCounterCount(CounterType.CHARGE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
    }

    @Test
    @DisplayName("Sacrificing Shrine can damage a planeswalker")
    void sacrificeDealsDamageToPlaneswalker() {
        Permanent shrine = addReadyShrine(player1);
        shrine.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent karn = harness.addToBattlefieldAndReturn(player2, new KarnLiberated());
        karn.setCounterCount(CounterType.LOYALTY, 6);

        harness.activateAbility(player1, 0, null, karn.getId());
        harness.passBothPriorities();

        assertThat(karn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Karn Liberated");
        harness.assertInGraveyard(player1, "Shrine of Burning Rage");
    }

    private Permanent addReadyShrine(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new ShrineOfBurningRage());
        perm.setSummoningSick(false);
        return perm;
    }
}
