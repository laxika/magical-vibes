package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrimstoneVandal.class})
class BrimstoneVandalTest extends BaseCardTest {

    @Test
    void becomesDayAsItEntersWhenThereIsNoDesignation() {
        harness.setHand(player1, List.of(new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void dealsDamageToEachOpponentWhenDayBecomesNight() {
        gd.dayNight = DayNight.DAY;
        harness.addToBattlefield(player1, new BrimstoneVandal());

        makeItNight();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void dealsDamageToEachOpponentWhenNightBecomesDay() {
        gd.dayNight = DayNight.NIGHT;
        gd.spellsCastLastTurn.put(player1.getId(), 2);
        harness.addToBattlefield(player1, new BrimstoneVandal());

        makeItDay();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    private void makeItNight() {
        harness.performUntapStep(player1);
        harness.passBothPriorities();
    }

    private void makeItDay() {
        harness.performUntapStep(player2);
        harness.passBothPriorities();
    }

    @Test
    void enteringAtNightDoesNotMakeItDayOrDealDamage() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void enteringDuringDayDoesNotDealDamage() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void staysDayAfterThePreviousActivePlayerCastsOneSpell() {
        gd.dayNight = DayNight.DAY;
        harness.setHand(player1, List.of(new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void becomesDayAfterThePreviousActivePlayerCastsTwoSpells() {
        gd.dayNight = DayNight.NIGHT;
        harness.setHand(player1, List.of(new BrimstoneVandal(), new BrimstoneVandal()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    void cannotBeBlockedByOnlyOneCreature() {
        addCreatureReady(player1, new BrimstoneVandal());
        addCreatureReady(player2, new BrimstoneVandal());
        addCreatureReady(player2, new BrimstoneVandal());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("two or more creatures");
    }

    @Test
    void canBeBlockedByTwoCreatures() {
        addCreatureReady(player1, new BrimstoneVandal());
        var firstBlocker = addCreatureReady(player2, new BrimstoneVandal());
        var secondBlocker = addCreatureReady(player2, new BrimstoneVandal());

        declareAttackersAndPrepareBlockers(List.of(0));
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2,
                        List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))));

        assertThat(firstBlocker.isBlocking()).isTrue();
        assertThat(secondBlocker.isBlocking()).isTrue();
        harness.assertLife(player2, 20);
    }
}
