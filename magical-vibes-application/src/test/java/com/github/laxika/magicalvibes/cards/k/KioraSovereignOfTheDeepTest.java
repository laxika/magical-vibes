package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantOctopus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KioraSovereignOfTheDeep.class, GiantOctopus.class, GrizzlyBears.class,
        LlanowarElves.class, Forest.class, Mountain.class})
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    private void setupKiora() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new KioraSovereignOfTheDeep());
    }

    private void castGiantOctopus() {
        harness.setHand(player1, List.of(new GiantOctopus()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0);
    }
}
