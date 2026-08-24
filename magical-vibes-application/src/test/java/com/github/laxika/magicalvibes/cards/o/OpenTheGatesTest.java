package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OpenTheGatesTest extends BaseCardTest {

    @Test
    void searchesForABasicLandOrGateAndPutsItIntoHand() {
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of(new Forest(), new RakdosGuildgate(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Rakdos Guildgate");

        int gateIndex = offered.indexOf(offered.stream()
                .filter(card -> card.getName().equals("Rakdos Guildgate"))
                .findFirst()
                .orElseThrow());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(gateIndex));

        harness.assertInHand(player1, "Rakdos Guildgate");
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .noneMatch(card -> card.getName().equals("Rakdos Guildgate"));
    }
}
