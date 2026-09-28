package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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
        GrizzlyBears.class, Shock.class})
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

    private void castPact(java.util.UUID targetPlayerId) {
        harness.setHand(player1, List.of(new PactOfTheSerpent()));
        addPactMana();
        harness.castSorcery(player1, 0, targetPlayerId);
        harness.passBothPriorities();
    }

    private void addPactMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Card> libraryWithFiveCards() {
        return List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock());
    }
}
