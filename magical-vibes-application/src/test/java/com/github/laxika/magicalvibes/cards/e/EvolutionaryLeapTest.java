package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EvolutionaryLeap.class, GrizzlyBears.class, FountainOfYouth.class})
class EvolutionaryLeapTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices a creature and puts the first revealed creature card into hand, rest on the bottom")
    void revealsUntilCreatureAndPutsItIntoHand() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setLibrary(player1, List.of(
                new FountainOfYouth(),
                new GrizzlyBears(),
                new EvolutionaryLeap()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Fountain of Youth", "Evolutionary Leap");
    }

    @Test
    @DisplayName("Puts every revealed card on the bottom when no creature card is found")
    void noCreatureFoundBottomsEverything() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.setLibrary(player1, List.of(new FountainOfYouth(), new EvolutionaryLeap()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Fountain of Youth");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Fountain of Youth", "Evolutionary Leap");
    }

    @Test
    void creatureOnTopLeavesUnrevealedLibraryInOrder() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        GrizzlyBears found = new GrizzlyBears();
        FountainOfYouth next = new FountainOfYouth();
        EvolutionaryLeap last = new EvolutionaryLeap();
        harness.setLibrary(player1, List.of(found, next, last));

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, last);
    }

    @Test
    void emptyLibraryStillRequiresSacrificeAndDoesNotCauseLoss() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    void revealedNoncreaturesGoBelowUnrevealedCards() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        FountainOfYouth revealedArtifact = new FountainOfYouth();
        EvolutionaryLeap revealedEnchantment = new EvolutionaryLeap();
        GrizzlyBears found = new GrizzlyBears();
        GrizzlyBears unrevealed = new GrizzlyBears();
        harness.setLibrary(player1, List.of(revealedArtifact, revealedEnchantment, found, unrevealed));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(found).doesNotContain(unrevealed);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.getFirst()).isSameAs(unrevealed);
        assertThat(library.subList(1, 3)).containsExactlyInAnyOrder(revealedArtifact, revealedEnchantment);
    }

    @Test
    void cannotActivateUsingOpponentsCreature() {
        harness.addToBattlefield(player1, new EvolutionaryLeap());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
