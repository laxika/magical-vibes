package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvendoWakingHaven.class, IntrepidTenderfoot.class})
class EvendoWakingHavenTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new EvendoWakingHaven()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Evendo, Waking Haven").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one green mana")
    void tapAbilityAddsGreenMana() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());

        harness.activateAbility(player1, 0, 1, null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Twelve charge counters unlock the creature-scaled mana ability")
    void twelveChargeCountersAddManaForEachCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 12);
        addCreatureReady(player1, new IntrepidTenderfoot());
        addCreatureReady(player1, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creature-scaled mana ability requires twelve charge counters")
    void creatureScaledManaAbilityRequiresTwelveChargeCounters() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 11);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("charge counters");
    }

    @Test
    @DisplayName("Station can tap a summoning-sick creature while the Planet is tapped")
    void stationUsesSummoningSickCreatureWhilePlanetIsTapped() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(creature.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isZero();
        harness.passBothPriorities();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(haven.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Station lets the controller choose between multiple untapped creatures")
    void stationChoosesCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent first = addCreatureReady(player1, new IntrepidTenderfoot());
        Permanent second = addCreatureReady(player1, new IntrepidTenderfoot());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Station cannot use an opponent's creature or a tapped creature")
    void stationRequiresUntappedControlledCreature() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent own = addCreatureReady(player1, new IntrepidTenderfoot());
        own.tap();
        Permanent opponent = addCreatureReady(player2, new IntrepidTenderfoot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponent.isTapped()).isFalse();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Station is restricted to the controller's main phase")
    void stationCannotActivateDuringCombat() {
        harness.addToBattlefield(player1, new EvendoWakingHaven());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Station cannot activate while another station ability is on the stack")
    void stationRequiresEmptyStack() {
        harness.addToBattlefield(player1, new EvendoWakingHaven());
        Permanent first = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.activateAbility(player1, 0, 1, null, null);
        Permanent second = addCreatureReady(player1, new IntrepidTenderfoot());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Unlocked mana ability counts only controlled creatures and resolves immediately")
    void scaledManaCountsOnlyControlledCreatures() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 13);
        Permanent own = addCreatureReady(player1, new IntrepidTenderfoot());
        own.tap();
        addCreatureReady(player2, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(haven.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(13);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unlocked mana ability can be activated with no creatures and still pays its costs")
    void scaledManaWithNoCreatures() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 12);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(haven.isTapped()).isTrue();
        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unlocked mana ability requires green mana to activate")
    void scaledManaRequiresGreenPayment() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 12);
        addCreatureReady(player1, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(haven.isTapped()).isFalse();
    }
    @Test
    @DisplayName("Station uses the creature's last known power if it leaves before resolution")
    void stationUsesLastKnownPower() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        harness.passBothPriorities();

        assertThat(haven.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Intrepid Tenderfoot");
    }

    @Test
    @DisplayName("Unlocked mana ability cannot activate while the Planet is tapped")
    void scaledManaRequiresUntappedPlanet() {
        Permanent haven = harness.addToBattlefieldAndReturn(player1, new EvendoWakingHaven());
        haven.setCounterCount(CounterType.CHARGE, 12);
        haven.tap();
        addCreatureReady(player1, new IntrepidTenderfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }
}
