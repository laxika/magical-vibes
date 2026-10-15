package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.a.AlmsCollector;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnderrealmLich;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PactOfTheSerpent.class, AvianChangeling.class, FountainOfYouth.class,
        GrizzlyBears.class, Shock.class, AlmsCollector.class, UnderrealmLich.class})
class PactOfTheSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws and loses life for each creature of the chosen type")
    void targetPlayerDrawsAndLosesLifePerChosenType() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A Changeling controlled by the target player counts as the chosen type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player2, new AvianChangeling());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creatures controlled by other players are not counted")
    void onlyTargetPlayersCreaturesCount() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The chosen type count excludes noncreature permanents")
    void onlyCreaturesCount() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature type with no matches has no effect")
    void chosenTypeWithNoMatchesDoesNothing() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "GOBLIN");

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void cannotTargetPermanent() {
        var artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new PactOfTheSerpent()));
        addPactMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("This spell can only target players");
    }

    @Test
    @DisplayName("The caster can target themselves and chooses the creature type during resolution")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setLife(player1, 20);

        castPact(player1.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Creatures removed in response do not contribute to X")
    void countsCreaturesAtResolution() {
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of(new Shock()));
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PactOfTheSerpent()));
        addPactMana();
        harness.castSorcery(player1, 0, player2.getId());
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Alms Collector replaces the whole draw instruction without reducing the life loss")
    void multiCardDrawIsReplacedByAlmsCollector() {
        harness.addToBattlefield(player1, new AlmsCollector());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, libraryWithFiveCards());
        harness.setLibrary(player2, libraryWithFiveCards());
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "BEAR");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Life loss occurs after the draw replacement has finished")
    void waitsForDrawReplacementBeforeLosingLife() {
        harness.addToBattlefield(player2, new UnderrealmLich());
        List<Card> library = libraryWithFiveCards();
        harness.setLibrary(player2, library);
        harness.setHand(player2, List.of());
        harness.setLife(player2, 20);

        castPact(player2.getId());
        harness.handleListChoice(player1, "ZOMBIE");

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertLife(player2, 20);
        harness.handleMultipleCardsChosen(player2, List.of(library.getFirst().getId()));

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(library.getFirst());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 19);
    }

    private void castPact(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PactOfTheSerpent()));
        addPactMana();
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }

    private void addPactMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
    }
}
