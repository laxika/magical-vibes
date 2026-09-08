package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkywarpSkaab.class, GrizzlyBears.class, Shock.class, Island.class})
class SkywarpSkaabTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability exiles two creature cards and draws a card")
    void exilesTwoCreaturesAndDraws() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card noncreature = new Shock();
        Card draw = new Island();
        harness.setGraveyard(player1, List.of(first, second, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(noncreature);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Declining the ETB ability does not exile or draw")
    void decliningDoesNothing() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card draw = new Island();
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
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card third = new GrizzlyBears();
        Card noncreature = new Shock();
        Card draw = new Island();
        harness.setGraveyard(player1, List.of(first, second, third, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId(), third.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(third, noncreature);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("Fewer than two creature cards cannot pay the optional exile")
    void fewerThanTwoCreaturesDoNothing() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card draw = new Island();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setLibrary(player1, List.of(draw));

        castSkywarpSkaab();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, noncreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(draw);
    }

    private void castSkywarpSkaab() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SkywarpSkaab()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
