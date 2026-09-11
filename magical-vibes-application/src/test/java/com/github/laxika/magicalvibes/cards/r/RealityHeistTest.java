package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RealityHeist.class, Ornithopter.class, GrizzlyBears.class, Shock.class})
class RealityHeistTest extends BaseCardTest {

    @Test
    void affinityForArtifactsReducesGenericCost() {
        harness.setHand(player1, List.of(new RealityHeist()));
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addToBattlefield(player1, new Ornithopter());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Reality Heist");
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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

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

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(creature1, instant1, creature2, instant2,
                        creature3, instant3, creature4, belowTopSeven);
    }

    private void addFullMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
