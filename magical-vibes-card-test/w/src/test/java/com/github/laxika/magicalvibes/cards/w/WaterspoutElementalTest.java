package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaterspoutElemental.class, GrizzlyBears.class, Island.class, Terminate.class})
class WaterspoutElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Without kicker, it does not return creatures or skip a turn")
    void withoutKickerDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Kicker returns all other creatures, leaves itself and noncreatures, and skips a turn")
    void kickedEtbReturnsOtherCreaturesAndSkipsTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        harness.assertOnBattlefield(player1, "Island");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kicker skips a turn even when there are no other creatures")
    void kickedWithNoOtherCreaturesStillSkipsTurn() {
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Kicker returns another Waterspout Elemental but keeps its own source")
    void kickedReturnsOtherElementalWithSameName() {
        harness.addToBattlefield(player2, new WaterspoutElemental());
        harness.setHand(player1, List.of(new WaterspoutElemental()));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Waterspout Elemental");
        harness.assertNotOnBattlefield(player2, "Waterspout Elemental");
        harness.assertInHand(player2, "Waterspout Elemental");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing the source does not stop its kicked ability or change who skips a turn")
    void kickedAbilityResolvesAfterSourceIsDestroyed() {
        harness.addToBattlefield(player2, new WaterspoutElemental());
        harness.setHand(player1, List.of(new WaterspoutElemental(), new Terminate()));
        addBaseMana();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        var source = findPermanent(player1, "Waterspout Elemental");
        harness.castAndResolveInstant(player1, 0, source.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Waterspout Elemental");
        harness.assertNotOnBattlefield(player2, "Waterspout Elemental");
        harness.assertInHand(player2, "Waterspout Elemental");
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isZero();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
