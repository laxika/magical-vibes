package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LammastideWeave.class, Mulldrifter.class, LeylineOfTheVoid.class, BruvacTheGrandiloquent.class})
class LammastideWeaveTest extends BaseCardTest {

    private void cast(com.github.laxika.magicalvibes.model.Player targetPlayer) {
        harness.setHand(player1, List.of(new LammastideWeave()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, targetPlayer.getId());
    }

    @Test
    @DisplayName("Resolving prompts the controller to name a card")
    void promptsControllerToNameCard() {
        cast(player2);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.NameCardMillGainLifeChoice.class);
    }

    @Test
    @DisplayName("Milling the named card gains life equal to its mana value and draws a card")
    void matchGainsLifeAndDraws() {
        Card top = new Mulldrifter();
        gd.playerDecks.get(player2.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Mulldrifter");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Milling a card that doesn't match the name still mills and draws but gains no life")
    void mismatchMillsAndDrawsWithoutLife() {
        Card top = new Mulldrifter();
        gd.playerDecks.get(player2.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Lammastide Weave");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Can target yourself: mills your own card and gains life on a match")
    void canTargetSelf() {
        Card top = new LammastideWeave();
        gd.playerDecks.get(player1.getId()).addFirst(top);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player1);
        harness.handleListChoice(player1, "Lammastide Weave");

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Interaction clears and spell leaves the stack after resolving")
    void interactionClearsAfterResolve() {
        Card top = new Mulldrifter();
        gd.playerDecks.get(player2.getId()).addFirst(top);

        cast(player2);
        harness.handleListChoice(player1, "Mulldrifter");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void emptyTargetLibraryStillDrawsWithoutGainingLife() {
        harness.setLibrary(player2, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Lammastide Weave");

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsOnlyOneCardAndDrawsAfterSelfMill() {
        Card milled = new LammastideWeave();
        Card drawn = new Mulldrifter();
        harness.setLibrary(player1, List.of(milled, drawn, new Mulldrifter()));

        cast(player1);
        harness.handleListChoice(player1, "Lammastide Weave");

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(milled).doesNotContain(drawn);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({LeylineOfTheVoid.class})
    void namedCardMilledIntoExileStillGainsLife() {
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Card top = new Mulldrifter();
        harness.setLibrary(player2, List.of(top));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Mulldrifter");

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(top);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertLife(player1, lifeBefore + 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({BruvacTheGrandiloquent.class})
    void namedSecondCardMilledByReplacementAlsoGainsLife() {
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        Card first = new LammastideWeave();
        Card second = new Mulldrifter();
        harness.setLibrary(player2, List.of(first, second));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Mulldrifter");

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        harness.assertLife(player1, lifeBefore + 5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @CardUsed({BruvacTheGrandiloquent.class})
    void gainsCombinedManaValueWhenReplacementMillsTwoNamedCards() {
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        Card first = new Mulldrifter();
        Card second = new Mulldrifter();
        harness.setLibrary(player2, List.of(first, second));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(player2);
        harness.handleListChoice(player1, "Mulldrifter");

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second);
        harness.assertLife(player1, lifeBefore + 10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void namePromptOffersLegalNamesAbsentFromTheGame() {
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new LammastideWeave()));
        harness.setLibrary(player2, List.of(new LammastideWeave()));
        harness.clearMessages();

        cast(player2);

        assertThat(harness.getConn1().getMessagesContaining("Choose a card name."))
                .anyMatch(message -> message.contains("Mulldrifter"));
    }
}
