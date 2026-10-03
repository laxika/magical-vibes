package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CullingDais.class, CarapaceForger.class})
class CullingDaisTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature adds a charge counter to Culling Dais")
    void sacrificeCreatureAddsChargeCounter() {
        Permanent dais = addReadyDais(player1);
        harness.addToBattlefield(player1, new CarapaceForger());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack.getFirst().isNonTargeting()).isTrue();
        harness.passBothPriorities(); // resolve ability

        // Carapace Forger should be sacrificed
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        harness.assertInGraveyard(player1, "Carapace Forger");

        // Culling Dais should have 1 charge counter
        assertThat(dais.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability 0 requires tap")
    void ability0RequiresTap() {
        Permanent dais = addReadyDais(player1);
        dais.tap();
        harness.addToBattlefield(player1, new CarapaceForger());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple sacrifices accumulate charge counters")
    void multipleSacrificesAccumulateCounters() {
        Permanent dais = addReadyDais(player1);

        // First sacrifice
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dais.getCounterCount(CounterType.CHARGE)).isEqualTo(1);

        // Untap Culling Dais for second activation
        dais.untap();

        // Second sacrifice
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dais.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Culling Dais draws cards equal to charge counters")
    void sacrificeSelfDrawsCardsEqualToChargeCounters() {
        Permanent dais = addReadyDais(player1);
        dais.setCounterCount(CounterType.CHARGE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities(); // resolve ability

        // Culling Dais should be in the graveyard
        harness.assertNotOnBattlefield(player1, "Culling Dais");
        harness.assertInGraveyard(player1, "Culling Dais");

        // Should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Sacrificing Culling Dais with 0 counters draws 0 cards")
    void sacrificeSelfWithZeroCountersDrawsNothing() {
        addReadyDais(player1);
        // No charge counters
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        // Culling Dais should be in the graveyard
        harness.assertNotOnBattlefield(player1, "Culling Dais");

        // Should not have drawn any cards
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Culling Dais goes to graveyard after sacrifice")
    void cullingDaisGoesToGraveyardAfterSacrifice() {
        addReadyDais(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        // Should be in graveyard immediately (sacrifice is a cost)
        harness.assertNotOnBattlefield(player1, "Culling Dais");
        harness.assertInGraveyard(player1, "Culling Dais");
    }

    @Test
    @DisplayName("Creature sacrifice and tap are paid before the charge counter resolves")
    void sacrificeAndTapAreImmediateCosts() {
        Permanent dais = addReadyDais(player1);
        harness.addToBattlefield(player1, new CarapaceForger());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Carapace Forger");
        harness.assertNotOnBattlefield(player1, "Carapace Forger");
        assertThat(dais.isTapped()).isTrue();
        assertThat(dais.getCounterCount(CounterType.CHARGE)).isZero();

        harness.passBothPriorities();

        assertThat(dais.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsCreature() {
        Permanent dais = addReadyDais(player1);
        harness.addToBattlefield(player2, new CarapaceForger());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> harness.activateAbility(player1, 0, null, null)
        ).isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Carapace Forger");
        assertThat(dais.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The draw ability can be activated while tapped and ignores other counter types")
    void drawAbilityWorksWhileTappedAndCountsOnlyChargeCounters() {
        Permanent dais = addReadyDais(player1);
        dais.tap();
        dais.setCounterCount(CounterType.CHARGE, 2);
        dais.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Culling Dais");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("Sacrificing Dais in response draws only its existing counters")
    void pendingCounterAbilityDoesNotIncreaseDrawAfterSacrifice() {
        Permanent dais = addReadyDais(player1);
        dais.setCounterCount(CounterType.CHARGE, 2);
        harness.addToBattlefield(player1, new CarapaceForger());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Culling Dais");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyDais(Player player) {
        return addCreatureReady(player, new CullingDais());
    }
}
