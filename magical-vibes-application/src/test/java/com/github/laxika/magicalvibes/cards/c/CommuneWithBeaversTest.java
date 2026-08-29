package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CommuneWithBeavers.class, DarksteelIngot.class, Forest.class, GrizzlyBears.class, Shock.class})
class CommuneWithBeaversTest extends BaseCardTest {

    @Test
    void offersArtifactCreatureAndLandCardsFromTheTopThree() {
        Card artifact = new DarksteelIngot();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new Shock();
        setLibrary(artifact, creature, land, instant);

        castAndResolve();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice.params().cards()).containsExactlyInAnyOrder(artifact, creature, land);
        assertThat(choice.params().canFailToFind()).isTrue();
    }

    @Test
    void putsChosenCardIntoHandAndOrdersTheRestOnTheBottom() {
        Card artifact = new DarksteelIngot();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        setLibrary(artifact, creature, land);

        castAndResolve();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineAndPutAllLookedAtCardsOnTheBottom() {
        Card artifact = new DarksteelIngot();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        setLibrary(artifact, creature, land);

        castAndResolve();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land, creature, artifact);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new CommuneWithBeavers()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }
}
