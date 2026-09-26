package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BuxtonDecoratedHost.class, GrizzlyBears.class, LlanowarElves.class, Forest.class})
class BuxtonDecoratedHostTest extends BaseCardTest {

    @Test
    @DisplayName("Seeks a nonland permanent within the mana-value bound and puts it onto the battlefield")
    void seeksPermanentBasedOnTappedCreatureCount() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setLibrary(player1, List.of(new LlanowarElves(), new GrizzlyBears(), new Forest()));

        resolveEndStep(player1);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(findNamedPermanent(player1, "Llanowar Elves").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Counts only tapped creatures for X")
    void requiresEnoughTappedCreaturesForHigherManaValue() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        resolveEndStep(player1);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger without a tapped creature")
    void doesNotTriggerWithoutTappedCreature() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    @Test
    @DisplayName("Intervening-if fails if the tapped creature untaps before resolution")
    void interveningIfFailsAtResolution() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        Permanent tappedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedCreature.tap();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player1);
        tappedCreature.untap();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Llanowar Elves");
    }

    private void resolveEndStep(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findNamedPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
