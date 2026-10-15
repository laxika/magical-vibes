package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GatherSpecimens;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
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

@CardUsed({LupineHarbingers.class, GatherSpecimens.class})
class LupineHarbingersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one +1/+1 counter after one of its controller's turns begins")
    void entersWithCountersForTurnsBegunSinceForetell() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, lupine.getId());
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Lupine Harbingers");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enters without counters when it was not foretold")
    void hardCastEntersWithoutCounters() {
        harness.setHand(player1, List.of(new LupineHarbingers()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Lupine Harbingers");
        assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Counts each later controller turn but not intervening opponent turns")
    void waitingThreeControllerTurnsGivesThreeCounters() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.setLibrary(player1, List.of(new LupineHarbingers(), new LupineHarbingers(),
                new LupineHarbingers()));
        harness.setLibrary(player2, List.of(new LupineHarbingers(), new LupineHarbingers(),
                new LupineHarbingers()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);

        for (int turn = 0; turn < 3; turn++) {
            harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
            harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, lupine.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Lupine Harbingers")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot cast a foretold card during the turn it was foretold")
    void cannotCastOnForetellTurn() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, lupine.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Lupine Harbingers");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Casting from foretell requires six mana rather than the normal four")
    void normalManaCostCannotPayForetellCost() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, lupine.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertNotOnBattlefield(player1, "Lupine Harbingers");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counts the entering controller's turns since foretell when entry is redirected")
    void redirectedEntryCountsNewControllersTurnsSinceForetell() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        harness.setHand(player2, List.of(new GatherSpecimens()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, lupine.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lupine Harbingers");
        assertThat(findPermanent(player2, "Lupine Harbingers")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can attack immediately and trample excess damage over a blocker")
    void foretoldCreatureHasHasteAndTramplesWithItsEntryCounter() {
        LupineHarbingers lupine = new LupineHarbingers();
        harness.setHand(player1, List.of(lupine));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new LupineHarbingers());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, lupine.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 19);
        harness.assertOnBattlefield(player1, "Lupine Harbingers");
        harness.assertInGraveyard(player2, "Lupine Harbingers");
    }
}
