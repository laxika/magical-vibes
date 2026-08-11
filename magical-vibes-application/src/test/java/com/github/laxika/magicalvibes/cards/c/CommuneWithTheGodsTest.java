package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommuneWithTheGodsTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and enchantment cards among the revealed five")
    void offersCreatureOrEnchantment() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Pacifism(), new HillGiant(), new Forest());

        resolveCommune();

        GameData data = harness.getGameData();
        assertThat(data.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(data)).containsExactlyInAnyOrder("Grizzly Bears", "Pacifism", "Hill Giant");
        assertThat(data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Taking an enchantment puts it in hand and the rest into the graveyard")
    void takingEnchantmentRestToGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card pacifism = new Pacifism();
        Card giant = new HillGiant();
        Card forest = new Forest();
        setupTopFive(bears, shock, pacifism, giant, forest);

        resolveCommune();
        chooseCard(searchCards(gd).indexOf("Pacifism"));

        assertThat(gd.playerHands.get(player1.getId())).contains(pacifism);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears, shock, giant, forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining still puts all five revealed cards into the graveyard")
    void decliningBinsEverything() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Pacifism(), new HillGiant(), new Forest());

        resolveCommune();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Shock", "Pacifism", "Hill Giant", "Forest");
    }

    @Test
    @DisplayName("With no creatures or enchantments, all five go to the graveyard")
    void noEligibleBinsDirectly() {
        setupTopFive(new Shock(), new Forest(), new Shock(), new Forest(), new Shock());

        resolveCommune();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Game log records the public reveal")
    void gameLogRecordsReveal() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Pacifism(), new Shock(), new Forest());

        resolveCommune();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Commune with the Gods"));
    }

    private void resolveCommune() {
        harness.setHand(player1, List.of(new CommuneWithTheGods()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData data) {
        return data.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
