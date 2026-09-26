package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrizzledHuntmaster.class, GrizzlyBears.class, Forest.class})
class GrizzledHuntmasterTest extends BaseCardTest {

    @Test
    void exilesSameNamedCardsAndConjuresOneDuplicatePerHandCardExiled() {
        GrizzlyBears chosenFromHand = new GrizzlyBears();
        GrizzlyBears additionalHandCopy = new GrizzlyBears();
        GrizzlyBears libraryCopy = new GrizzlyBears();
        GrizzlyBears outsideCreature = new GrizzlyBears();

        harness.setHand(player1, List.of(new GrizzledHuntmaster(), chosenFromHand, additionalHandCopy));
        harness.setLibrary(player1, List.of(new Forest(), libraryCopy));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(handChoice).isNotNull();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenFromHand));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(additionalHandCopy.getId(), libraryCopy.getId()));

        PendingInteraction.SearchOutsideGameOrExileCardChoice outsideChoice =
                gd.interaction.activeInteraction(PendingInteraction.SearchOutsideGameOrExileCardChoice.class);
        assertThat(outsideChoice).isNotNull();
        assertThat(outsideChoice.mandatory()).isTrue();
        assertThat(outsideChoice.validCardIds()).containsExactly(outsideCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(outsideCreature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenFromHand, additionalHandCopy, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Grizzly Bears"))).hasSize(2);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineTheInitialCreatureExile() {
        GrizzlyBears creature = new GrizzlyBears();
        GrizzlyBears outsideCreature = new GrizzlyBears();
        harness.setHand(player1, List.of(new GrizzledHuntmaster(), creature));
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(outsideCreature)));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(outsideCreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
