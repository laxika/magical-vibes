package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetworkTerminal;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RealityHeist.class, Ornithopter.class, GrizzlyBears.class, Shock.class, NetworkTerminal.class})
class RealityHeistTest extends BaseCardTest {

    @Test
    void affinityForArtifactsReducesGenericCost() {
        harness.setHand(player1, List.of(new RealityHeist()));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(RealityHeist.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void putsUpToTwoArtifactsFromTopSevenIntoHand() {
        Card artifact1 = new Ornithopter();
        Card artifact2 = new Ornithopter();
        Card artifact3 = new Ornithopter();
        Card creature1 = new GrizzlyBears();
        Card instant1 = new Shock();
        Card creature2 = new GrizzlyBears();
        Card instant2 = new Shock();
        Card belowTopSeven = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact1, creature1, artifact2, instant1,
                artifact3, creature2, instant2, belowTopSeven));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact1.getId(), artifact2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact1, artifact2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(artifact3, creature1, instant1, creature2, instant2, belowTopSeven);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void bottomsAllCardsWhenNoArtifactIsAmongTopSeven() {
        Card creature1 = new GrizzlyBears();
        Card instant1 = new Shock();
        Card creature2 = new GrizzlyBears();
        Card instant2 = new Shock();
        Card creature3 = new GrizzlyBears();
        Card instant3 = new Shock();
        Card creature4 = new GrizzlyBears();
        Card belowTopSeven = new Ornithopter();
        harness.setLibrary(player1, List.of(creature1, instant1, creature2, instant2,
                creature3, instant3, creature4, belowTopSeven));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature1, instant1, creature2, instant2,
                        creature3, instant3, creature4, belowTopSeven);
    }

    @Test
    void mayDeclineArtifactsEvenWhenOnlyTwoAreAvailable() {
        Card artifact1 = new Ornithopter();
        Card artifact2 = new Ornithopter();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact1, other, artifact2));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(artifact1, artifact2, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void mayChooseOnlyOneArtifactAndPreservesCardsBelowTopSeven() {
        Card artifact1 = new Ornithopter();
        Card artifact2 = new Ornithopter();
        Card artifact3 = new Ornithopter();
        Card other1 = new GrizzlyBears();
        Card other2 = new GrizzlyBears();
        Card other3 = new Shock();
        Card other4 = new Shock();
        Card below1 = new Ornithopter();
        Card below2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact1, other1, artifact2, other2,
                artifact3, other3, other4, below1, below2));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact2);
        assertThat(gd.playerDecks.get(player1.getId())).startsWith(below1, below2);
        assertThat(gd.playerDecks.get(player1.getId()).subList(2, 8))
                .containsExactlyInAnyOrder(artifact1, artifact3, other1, other2, other3, other4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void rejectsNonArtifactsAndMoreThanTwoArtifacts() {
        Card artifact1 = new Ornithopter();
        Card artifact2 = new Ornithopter();
        Card artifact3 = new Ornithopter();
        Card other = new GrizzlyBears();
        harness.setLibrary(player1, List.of(artifact1, artifact2, artifact3, other));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(other.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(artifact1.getId(), artifact2.getId(), artifact3.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(artifact1.getId(), artifact3.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact1, artifact3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(artifact2, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canTakeTheOnlyArtifactFromAShortLibrary() {
        Card artifact = new Ornithopter();
        Card other = new Shock();
        harness.setLibrary(player1, List.of(other, artifact));
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void resolvesWithAnEmptyLibraryWithoutDrawing() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RealityHeist()));
        addFullMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void countsTappedNoncreatureArtifactsAndCanSelectNoncreatureArtifactCards() {
        Card artifact = new NetworkTerminal();
        harness.setLibrary(player1, List.of(artifact));
        harness.setHand(player1, List.of(new RealityHeist()));
        harness.addToBattlefield(player1, new NetworkTerminal());
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(true);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void affinityDoesNotCountOpponentsArtifactsOrNonArtifacts() {
        harness.setHand(player1, List.of(new RealityHeist()));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessAffinityCannotReduceTheBlueManaRequirement() {
        harness.setHand(player1, List.of(new RealityHeist()));
        for (int i = 0; i < 7; i++) {
            harness.addToBattlefield(player1, new Ornithopter());
        }
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
