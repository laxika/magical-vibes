package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SturdyHatchling.class, GrizzlyBears.class, FugitiveWizard.class, HillGiant.class,
        Snakeform.class})
class SturdyHatchlingTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with four -1/-1 counters (6/6 becomes 2/2)")
    void entersWithFourMinusCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SturdyHatchling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.stack).isEmpty();

        Permanent hatchling = findHatchling(player1);
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(hatchling.getEffectivePower()).isEqualTo(2);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a green spell removes a -1/-1 counter")
    void greenSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a blue spell removes a -1/-1 counter")
    void blueSpellRemovesCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new FugitiveWizard()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the removal trigger

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a non-green non-blue spell does not remove a counter")
    void redSpellDoesNotRemoveCounter() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).noneMatch(e -> e.getCard().getName().equals("Sturdy Hatchling"));
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Activating {G/U} grants shroud until end of turn, wears off at end of turn")
    void shroudGrantWearsOffAtEndOfTurn() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the grant

        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("A green-blue spell triggers twice even when paid for with only blue mana")
    void hybridSpellRemovesTwoCountersBeforeSpellResolves() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SturdyHatchling()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.passBothPriorities();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Sturdy Hatchling")).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Sturdy Hatchling")).hasSize(2);
        assertThat(findPermanents(player1, "Sturdy Hatchling").get(1)
                .getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's green-blue spell does not remove counters")
    void opponentSpellDoesNotRemoveCounters() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SturdyHatchling()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Both color triggers resolve with only one counter remaining")
    void removalCannotReduceCountersBelowZero() {
        Permanent hatchling = addReadyHatchling(player1);
        hatchling.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SturdyHatchling()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(hatchling.getEffectivePower()).isEqualTo(6);
        assertThat(hatchling.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Blue mana pays for shroud, and shroud does not stop further activations")
    void shroudCanBeActivatedAgainWithBlueMana() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, hatchling, Keyword.SHROUD)).isTrue();
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering without being cast still gives four counters and triggers no removal")
    void enteringWithoutCastingHasFourCounters() {
        Permanent existing = addReadyHatchling(player1);

        Permanent entering = harness.enterBattlefieldAndReturn(player1, new SturdyHatchling());

        assertThat(entering.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(existing.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud prevents both players from targeting the Hatchling")
    void shroudPreventsBothPlayersFromTargeting() {
        Permanent hatchling = addReadyHatchling(player1);
        addReadyHatchling(player2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Snakeform()));
        harness.setHand(player2, List.of(new Snakeform()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player2, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, hatchling.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("shroud");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, hatchling.getId()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud gained in response makes an already cast spell fail to resolve")
    void shroudInResponseStopsTargetedSpell() {
        Permanent hatchling = addReadyHatchling(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Snakeform()));
        harness.setLibrary(player2, List.of(new SturdyHatchling()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player2, 0, hatchling.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sturdy Hatchling");
        assertThat(hatchling.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(4);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Snakeform");
    }

    private Permanent addReadyHatchling(Player player) {
        Permanent perm = addCreatureReady(player, new SturdyHatchling());
        perm.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 4);
        return perm;
    }

    private Permanent findHatchling(Player player) {
        return findPermanent(player, "Sturdy Hatchling");
    }
}
