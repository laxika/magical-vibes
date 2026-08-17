package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CateranOverlordTest extends BaseCardTest {

    @Test
    void sacrificingACreatureRegeneratesCateranOverlord() {
        Permanent overlord = addCreatureReady(player1, new CateranOverlord());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID fodderId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodderId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(overlord.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void searchesForMercenaryPermanentWithManaValueAtMostSix() {
        addCreatureReady(player1, new CateranOverlord());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(new CateranEnforcer(), new GrizzlyBears(), new CateranOverlord()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Cateran Enforcer");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Cateran Enforcer");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }
}
