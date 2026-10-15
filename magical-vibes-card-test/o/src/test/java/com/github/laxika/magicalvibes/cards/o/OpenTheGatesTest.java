package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.cards.s.SteepleCreeper;
import com.github.laxika.magicalvibes.cards.s.StompingGround;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpenTheGates.class, Forest.class, RakdosGuildgate.class})
class OpenTheGatesTest extends BaseCardTest {

    @Test
    @CardUsed(GrizzlyBears.class)
    void searchesForABasicLandOrGateAndPutsItIntoHand() {
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of(new Forest(), new RakdosGuildgate(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Rakdos Guildgate");

        int gateIndex = offered.indexOf(offered.stream()
                .filter(card -> card.getName().equals("Rakdos Guildgate"))
                .findFirst()
                .orElseThrow());
        harness.handleCardChosen(player1, gateIndex);

        harness.assertInHand(player1, "Rakdos Guildgate");
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(2)
                .noneMatch(card -> card.getName().equals("Rakdos Guildgate"));
    }

    @Test
    @CardUsed({SteepleCreeper.class, StompingGround.class})
    void findsBasicLandButNotANonbasicLandWithBasicLandTypes() {
        Forest forest = new Forest();
        RakdosGuildgate gate = new RakdosGuildgate();
        StompingGround nonbasic = new StompingGround();
        SteepleCreeper creature = new SteepleCreeper();
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of(forest, gate, nonbasic, creature));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).containsExactlyInAnyOrder(forest, gate);
        harness.handleCardChosen(player1, offered.indexOf(forest));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(gate, nonbasic, creature);
        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("reveals Forest"));
    }

    @Test
    void mayFailToFindEvenWhenAnEligibleCardExists() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Open the Gates");
    }

    @Test
    @CardUsed({SteepleCreeper.class, StompingGround.class})
    void resolvesWhenLibraryHasNoEligibleCards() {
        StompingGround nonbasic = new StompingGround();
        SteepleCreeper creature = new SteepleCreeper();
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of(nonbasic, creature));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonbasic, creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Open the Gates");
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new OpenTheGates()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Open the Gates");
    }
}
