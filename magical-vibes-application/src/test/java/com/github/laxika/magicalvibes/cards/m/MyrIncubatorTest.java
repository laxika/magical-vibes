package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MyrIncubatorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of artifact cards and creates one Myr for each")
    void exilesArtifactsAndCreatesMatchingNumberOfMyrs() {
        resolveActivation(List.of(new MyrSire(), new GoldMyr(), new LlanowarElves()));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2);
        assertThat(search.params().cards()).allMatch(card -> card.hasType(CardType.ARTIFACT));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getName())
                .containsExactlyInAnyOrder("Myr Sire", "Gold Myr");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Llanowar Elves");
        assertThat(findPermanents(player1, "Myr")).hasSize(2);
        assertThat(findPermanent(player1, "Myr").getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(findPermanents(player1, "Myr"))
                .allMatch(permanent -> permanent.getCard().getAdditionalTypes().contains(CardType.ARTIFACT));
        harness.assertInGraveyard(player1, "Myr Incubator");
    }

    @Test
    @DisplayName("May stop the artifact search without selecting any card")
    void maySelectZeroArtifactCards() {
        resolveActivation(List.of(new MyrSire(), new GoldMyr()));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.exiledCards).isEmpty();
        assertThat(findPermanents(player1, "Myr")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Myr Incubator");
    }

    private void resolveActivation(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.setLibrary(player1, library);
        harness.addToBattlefield(player1, new MyrIncubator());
        Permanent incubator = findPermanent(player1, "Myr Incubator");
        incubator.setSummoningSick(false);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
