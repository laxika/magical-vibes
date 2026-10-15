package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AnHavvaTownship;
import com.github.laxika.magicalvibes.cards.a.AnabaShaman;
import com.github.laxika.magicalvibes.cards.d.DwarvenTrader;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiteAlchemist.class, DwarvenTrader.class, AnabaShaman.class, AnHavvaTownship.class})
class SamiteAlchemistTest extends BaseCardTest {

    private void addAlchemistReady() {
        addCreatureReady(player1, new SamiteAlchemist());
    }

    @Test
    @DisplayName("Shields target creature you control for 4, taps it and locks its next untap")
    void shieldsTapsAndLocksTarget() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new DwarvenTrader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dwarven Trader");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent trader = findPermanent(player1, "Dwarven Trader");
        assertThat(trader.getDamagePreventionShield()).isEqualTo(4);
        assertThat(trader.isTapped()).isTrue();
        assertThat(trader.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents damage dealt to the target creature later this turn")
    void preventsDamageToTarget() {
        addAlchemistReady();
        Permanent alchemist = findPermanent(player1, "Samite Alchemist");
        addCreatureReady(player1, new AnabaShaman());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, alchemist.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 1, null, alchemist.getId());
        harness.passBothPriorities();

        assertThat(alchemist.getMarkedDamage()).isZero();
        assertThat(alchemist.getDamagePreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Keeps the target tapped through its next untap step")
    void skipsTargetNextUntapStep() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new DwarvenTrader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Dwarven Trader");
        Permanent trader = findPermanent(player1, "Dwarven Trader");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        advanceToUpkeep(player1);

        assertThat(trader.isTapped()).isTrue();
        assertThat(trader.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotTargetOpponentCreature() {
        addAlchemistReady();
        harness.addToBattlefield(player2, new DwarvenTrader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Dwarven Trader");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying {W}{W}")
    void requiresManaCost() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new DwarvenTrader());

        UUID targetId = harness.getPermanentId(player1, "Dwarven Trader");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a controlled noncreature permanent")
    void cannotTargetControlledNoncreature() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new AnHavvaTownship());
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "An-Havva Township");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addAlchemistReady();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A protected creature changing controllers can untap during the opponent's untap step")
    void restrictionAppliesOnlyDuringAbilityControllersNextUntap() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new DwarvenTrader());
        Permanent trader = findPermanent(player1, "Dwarven Trader");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, trader.getId());
        harness.passBothPriorities();

        // Model a control change after resolution, preserving the same permanent and its effects.
        gd.playerBattlefields.get(player1.getId()).remove(trader);
        gd.playerBattlefields.get(player2.getId()).add(trader);
        advanceToUpkeep(player2);

        assertThat(trader.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The creature untaps normally after the one restricted untap step")
    void untapsOnFollowingUntapStep() {
        addAlchemistReady();
        harness.addToBattlefield(player1, new DwarvenTrader());
        Permanent trader = findPermanent(player1, "Dwarven Trader");
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, trader.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        assertThat(trader.isTapped()).isTrue();
        advanceToUpkeep(player1);
        assertThat(trader.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        addAlchemistReady();
        Permanent alchemist = findPermanent(player1, "Samite Alchemist");
        alchemist.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, alchemist.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot pay the tap cost with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new SamiteAlchemist());
        UUID alchemistId = harness.getPermanentId(player1, "Samite Alchemist");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, alchemistId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The shield prevents only four damage across successive sources")
    void shieldIsConsumedAcrossDamageEvents() {
        addAlchemistReady();
        Permanent alchemist = findPermanent(player1, "Samite Alchemist");
        for (int i = 0; i < 5; i++) {
            addCreatureReady(player1, new AnabaShaman());
        }
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.RED, 5);
        harness.activateAbility(player1, 0, null, alchemist.getId());
        harness.passBothPriorities();

        for (int i = 1; i <= 4; i++) {
            harness.activateAbility(player1, i, null, alchemist.getId());
            harness.passBothPriorities();
            assertThat(alchemist.getMarkedDamage()).isZero();
        }
        harness.activateAbility(player1, 5, null, alchemist.getId());
        harness.passBothPriorities();

        assertThat(alchemist.getMarkedDamage()).isEqualTo(1);
        assertThat(alchemist.getDamagePreventionShield()).isZero();
    }
}
