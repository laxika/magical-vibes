package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.c.CarapaceForger;
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
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TrigonOfCorruption.class, CarapaceForger.class})
class TrigonOfCorruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with 3 charge counters")
    void entersWithThreeChargeCounters() {
        harness.setHand(player1, List.of(new TrigonOfCorruption()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
    }

    @Test
    @DisplayName("First ability adds a charge counter when paying {B}{B}")
    void firstAbilityAddsChargeCounter() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 3);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    @Test
    @DisplayName("First ability cannot be activated without {B}{B} mana")
    void firstAbilityRequiresBlackMana() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        // No mana available
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability puts a -1/-1 counter on target creature")
    void secondAbilityPutsMinusCounter() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new CarapaceForger());

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID creatureId = harness.getPermanentId(player2, "Carapace Forger");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, creatureId);
        harness.passBothPriorities();

        // Charge counter removed
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // -1/-1 counter placed
        Permanent creature = findPermanent(player2, "Carapace Forger");
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability cannot be activated with 0 charge counters")
    void cannotActivateSecondAbilityWithNoCounters() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new CarapaceForger());

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID creatureId = harness.getPermanentId(player2, "Carapace Forger");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Second ability kills a 1/1 creature via -1/-1 counter")
    void secondAbilityKillsOneOneCreature() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());

        // Create a 1/1 by giving Carapace Forger (2/2) a -1/-1 counter
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        creature.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        UUID creatureId = creature.getId();

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, creatureId);
        harness.passBothPriorities();

        // The 1/1 gets another -1/-1 counter, becomes 0/0, and dies.
        harness.assertNotOnBattlefield(player2, "Carapace Forger");
        harness.assertInGraveyard(player2, "Carapace Forger");
    }

    @Test
    @DisplayName("Second ability fizzles when target creature is removed before resolution")
    void secondAbilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new CarapaceForger());

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID creatureId = harness.getPermanentId(player2, "Carapace Forger");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        harness.activateAbility(player1, trigonIndex, 1, null, creatureId);

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Counters are still removed (cost is paid on activation)
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(0);

        // Ability fizzles
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Cannot activate the second ability while tapped after using the first")
    void cannotUseSecondAbilityWhileTapped() {
        harness.addToBattlefield(player1, new TrigonOfCorruption());
        harness.addToBattlefield(player2, new CarapaceForger());

        Permanent trigon = findPermanent(player1, "Trigon of Corruption");
        trigon.setCounterCount(CounterType.CHARGE, 3);

        // Use first ability (tap to add counter)
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Trigon is tapped, so the second ability cannot be activated.
        assertThat(trigon.isTapped()).isTrue();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID creatureId = harness.getPermanentId(player2, "Carapace Forger");
        int trigonIndex = gd.playerBattlefields.get(player1.getId()).indexOf(trigon);
        assertThatThrownBy(() -> harness.activateAbility(player1, trigonIndex, 1, null, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Charge counter and tap costs are paid before the target receives its counter")
    void paysCostsBeforeResolution() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfCorruption());
        trigon.setCounterCount(CounterType.CHARGE, 3);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();

        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The second ability can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfCorruption());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The second ability cannot target a noncreature artifact")
    void cannotTargetNoncreature() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfCorruption());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        Permanent other = harness.addToBattlefieldAndReturn(player2, new TrigonOfCorruption());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(trigon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The second ability resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfCorruption());
        trigon.setCounterCount(CounterType.CHARGE, 1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(trigon);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The first ability replenishes an empty trigon on resolution")
    void replenishesEmptyTrigon() {
        Permanent trigon = harness.addToBattlefieldAndReturn(player1, new TrigonOfCorruption());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(trigon.isTapped()).isTrue();
        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(trigon.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
