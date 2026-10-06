package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RepositorySkaab;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.c.ChillOfTheGrave;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywarpSkaab.class, RepositorySkaab.class, ChillOfTheGrave.class, Plains.class})
class SkywarpSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles two creature cards and draws a card")
    void exilesTwoCreaturesAndDraws() {
        Card first = new RepositorySkaab();
        Card second = new RepositorySkaab();
        Card noncreature = new ChillOfTheGrave();
        Card draw = new Plains();
        harness.setGraveyard(player1, List.of(first, second, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(noncreature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability does not exile or draw")
    void decliningDoesNothing() {
        Card first = new RepositorySkaab();
        Card second = new RepositorySkaab();
        Card draw = new Plains();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
    }

    @Test
    @DisplayName("The graveyard choice offers only creature cards")
    void onlyCreatureCardsAreOffered() {
        Card first = new RepositorySkaab();
        Card second = new RepositorySkaab();
        Card third = new RepositorySkaab();
        Card noncreature = new ChillOfTheGrave();
        Card draw = new Plains();
        harness.setGraveyard(player1, List.of(first, second, third, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(third, noncreature);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Fewer than two creature cards cannot pay the optional exile")
    void fewerThanTwoCreaturesDoNothing() {
        Card creature = new RepositorySkaab();
        Card noncreature = new ChillOfTheGrave();
        Card draw = new Plains();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, noncreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
    }

    @Test
    @DisplayName("Creatures in the opponent's graveyard cannot pay the optional exile")
    void opponentsGraveyardCannotPay() {
        Card first = new RepositorySkaab();
        Card second = new RepositorySkaab();
        Card draw = new Plains();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(draw);
    }

    private void castSkywarpSkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SkywarpSkaab(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
