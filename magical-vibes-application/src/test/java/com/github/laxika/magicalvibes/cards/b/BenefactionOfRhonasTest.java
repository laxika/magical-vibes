package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BenefactionOfRhonasTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only the creature cards among the top five")
    void firstPickOffersCreatures() {
        setupTopFive(new GrizzlyBears(), new Shock(), new GloriousAnthem(), new Forest(), new HillGiant());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("Taking a creature and an enchantment puts both into hand; the rest go to the graveyard")
    void takesCreatureAndEnchantmentRestToGraveyard() {
        setupTopFive(new GrizzlyBears(), new Shock(), new GloriousAnthem(), new Forest(), new HillGiant());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        // Pick the creature (Grizzly Bears).
        chooseCard(gd, 0);

        // Second pick offers only the enchantment.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactly("Glorious Anthem");

        // Pick the enchantment (Glorious Anthem).
        chooseCard(gd, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Grizzly Bears", "Glorious Anthem");
        // The rest are binned, and no reorder interaction is opened (graveyard has no order).
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Shock", "Forest", "Hill Giant");
    }

    @Test
    @DisplayName("Cannot take two creatures — the second pick is enchantment-only")
    void cannotTakeTwoCreatures() {
        setupTopFive(new GrizzlyBears(), new GloriousAnthem(), new HillGiant(), new Shock(), new Forest());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, 0);

        assertThat(searchCards(gd)).doesNotContain("Hill Giant").containsExactly("Glorious Anthem");
    }

    @Test
    @DisplayName("Declining the creature still offers the enchantment pick")
    void decliningCreatureStillOffersEnchantment() {
        setupTopFive(new GrizzlyBears(), new Shock(), new GloriousAnthem(), new Forest(), new HillGiant());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactly("Glorious Anthem");
    }

    @Test
    @DisplayName("With no creatures, the enchantment pick begins directly")
    void noCreaturesGoesStraightToEnchantment() {
        setupTopFive(new GloriousAnthem(), new Shock(), new Forest(), new AngelicChorus(), new Forest());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Glorious Anthem", "Angelic Chorus");
    }

    @Test
    @DisplayName("Declining both picks bins all five into the graveyard")
    void decliningBothBinsEverything() {
        setupTopFive(new GrizzlyBears(), new GloriousAnthem(), new Shock(), new Forest(), new HillGiant());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, -1); // decline the creature
        chooseCard(gd, -1); // decline the enchantment

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Glorious Anthem", "Shock", "Forest", "Hill Giant");
    }

    @Test
    @DisplayName("With no creatures or enchantments, all five are binned directly")
    void noEligibleBinsToGraveyardDirectly() {
        setupTopFive(new Shock(), new Shock(), new Forest(), new Forest(), new Shock());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Five binned cards plus the resolved sorcery itself.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    private void resolveBenefaction() {
        harness.setHand(player1, List.of(new BenefactionOfRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(GameData gd, int index) {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
