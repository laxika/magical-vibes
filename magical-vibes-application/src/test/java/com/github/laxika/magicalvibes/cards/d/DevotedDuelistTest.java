package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevotedDuelist.class, DarkRitual.class})
class DevotedDuelistTest extends BaseCardTest {

    @Test
    void damagesEachOpponentOnTheSecondSpellOnly() {
        harness.addToBattlefield(player1, new DevotedDuelist());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int controllerLifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void countsItsOwnCastBeforeEnteringTheBattlefield() {
        harness.setHand(player1, List.of(new DevotedDuelist(), new DarkRitual()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player2, opponentLifeBefore);

        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore - 1);
    }

    @Test
    void doesNotTriggerForItsOwnCastAsTheSecondSpell() {
        harness.setHand(player1, List.of(new DarkRitual(), new DevotedDuelist(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player2, opponentLifeBefore);

        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    void opponentsSpellsNeitherTriggerNorCountTowardsControllersSecondSpell() {
        harness.addToBattlefield(player1, new DevotedDuelist());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);
        harness.assertLife(player2, opponentLifeBefore);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore - 1);
    }

    @Test
    void triggersAgainOnOpponentsTurnAfterSpellCountResets() {
        harness.addToBattlefield(player1, new DevotedDuelist());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.castAndResolveInstant(player1, 0);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore - 1);

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore - 1);
        harness.castAndResolveInstant(player1, 0);
        harness.assertLife(player2, opponentLifeBefore - 2);
    }
}
