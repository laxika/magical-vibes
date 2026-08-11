package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CaptainSisayTest extends BaseCardTest {

    private void setUpCaptainSisay() {
        harness.addToBattlefield(player1, new CaptainSisay());
        Permanent captain = findPermanent(player1, "Captain Sisay");
        captain.setSummoningSick(false);
        gd.playerDecks.get(player1.getId()).clear();
    }

    @Test
    @DisplayName("The tap ability offers only legendary cards and puts the chosen card into hand")
    void searchesForLegendaryCard() {
        setUpCaptainSisay();
        ArvadTheCursed arvad = new ArvadTheCursed();
        gd.playerDecks.get(player1.getId()).addAll(List.of(arvad, new GrizzlyBears()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Arvad the Cursed");
        assertThat(search.params().reveals()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Arvad the Cursed");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search does not offer nonlegendary cards")
    void doesNotOfferNonlegendaryCards() {
        setUpCaptainSisay();
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Grizzly Bears");
    }
}
