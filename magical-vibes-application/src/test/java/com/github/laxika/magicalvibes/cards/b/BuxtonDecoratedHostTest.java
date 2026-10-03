package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({BuxtonDecoratedHost.class, GrizzlyBears.class, LlanowarElves.class, Forest.class, GiantGrowth.class})
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
        assertThat(findPermanent(player1, "Llanowar Elves").isTapped()).isFalse();
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

    @Test
    void seeksExactlyOneCardWithMultipleTappedCreatures() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).tap();
        harness.setLibrary(player1, List.of(new LlanowarElves(), new LlanowarElves()));

        resolveEndStep(player1);

        assertThat(countPermanents(player1, "Llanowar Elves")).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void evaluatesManaValueBoundAtResolution() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        first.tap();
        second.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        advanceToEndStep(player1);
        second.untap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void ignoresOpponentsTappedCreatures() {
        harness.addToBattlefield(player1, new BuxtonDecoratedHost());
        harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).tap();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefieldAndReturn(player1, new BuxtonDecoratedHost()).tap();
        harness.setLibrary(player1, List.of(new LlanowarElves()));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void tappedHostCountsItselfButDoesNotSeekLandOrAnOverCostPermanent() {
        harness.addToBattlefieldAndReturn(player1, new BuxtonDecoratedHost()).tap();
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        resolveEndStep(player1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    void tappedHostCountsItselfAndExcludesNonpermanentCards() {
        harness.addToBattlefieldAndReturn(player1, new BuxtonDecoratedHost()).tap();
        harness.setLibrary(player1, List.of(new GiantGrowth(), new LlanowarElves()));

        resolveEndStep(player1);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName).containsExactly("Giant Growth");
    }

    @Test
    void canConvokeHostAndUseThoseTappedCreaturesAtEndStep() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BuxtonDecoratedHost()));
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithConvoke(player1, 0, List.of(), List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Buxton, Decorated Host");
        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();

        resolveEndStep(player1);

        harness.assertOnBattlefield(player1, "Llanowar Elves");
    }

    private void resolveEndStep(Player activePlayer) {
        advanceToEndStep(activePlayer);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
