package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KioraSovereignOfTheDeep.class, GiantOctopus.class, GrizzlyBears.class,
        LlanowarElves.class, Forest.class, Mountain.class, CatharticReunion.class})
class KioraSovereignOfTheDeepTest extends BaseCardTest {

    @Test
    void matchingCreatureSpellLooksAtTopSpellManaValueCards() {
        setupKiora();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new GiantOctopus(), bears, new Forest(), new Mountain(),
                new LlanowarElves()));

        castGiantOctopus();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(bears);
        assertThat(search.params().reveals()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void chosenSpellIsCastForFreeAndUnchosenCardsGoToBottom() {
        setupKiora();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        LlanowarElves elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(bears, forest, mountain, elves));

        castGiantOctopus();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard() == bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain, elves);
    }

    @Test
    void equalManaValueAndLandsAreNotCastable() {
        setupKiora();
        GiantOctopus equal = new GiantOctopus();
        LlanowarElves lower = new LlanowarElves();
        harness.setLibrary(player1, List.of(equal, new Forest(), new Mountain(), lower));

        castGiantOctopus();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(lower);
    }

    @Test
    void unrelatedCreatureSpellDoesNotTrigger() {
        setupKiora();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void decliningCastBottomsOnlyLookedAtCards() {
        setupKiora();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        LlanowarElves elves = new LlanowarElves();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(bears, forest, mountain, elves, untouched));

        castGiantOctopus();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(bears, forest, mountain, elves);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void noEligibleSpellBottomsLookedAtCardsWithoutChoice() {
        setupKiora();
        GiantOctopus equal = new GiantOctopus();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        KioraSovereignOfTheDeep higher = new KioraSovereignOfTheDeep();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(equal, forest, mountain, higher, untouched));

        castGiantOctopus();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 5))
                .containsExactlyInAnyOrder(equal, forest, mountain, higher);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void shortLibraryStillUsesTriggeringSpellManaValueForEligibility() {
        setupKiora();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));

        castGiantOctopus();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void opponentsMatchingSpellDoesNotTrigger() {
        setupKiora();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GiantOctopus(), "{3}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    void freeCastCannotPutSpellOnStackBeforeMandatoryDiscardIsPaid() {
        setupKiora();
        CatharticReunion reunion = new CatharticReunion();
        harness.setLibrary(player1, List.of(reunion));
        castGiantOctopus();
        Forest firstDiscard = new Forest();
        Forest secondDiscard = new Forest();
        harness.setHand(player1, List.of(firstDiscard, secondDiscard));

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == reunion
                && (gd.playerHands.get(player1.getId()).contains(firstDiscard)
                || gd.playerHands.get(player1.getId()).contains(secondDiscard)));
    }

    private void setupKiora() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new KioraSovereignOfTheDeep());
    }

    private void castGiantOctopus() {
        harness.castFromHand(player1, new GiantOctopus(), "{3}{U}");
    }
}
