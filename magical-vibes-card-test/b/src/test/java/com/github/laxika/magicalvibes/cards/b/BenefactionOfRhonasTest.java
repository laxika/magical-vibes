package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CartoucheOfSolidarity;
import com.github.laxika.magicalvibes.cards.c.Colossapede;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.cards.o.OashraCultivator;
import com.github.laxika.magicalvibes.cards.t.TrialOfSolidarity;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenefactionOfRhonas.class, Colossapede.class, OashraCultivator.class,
        CartoucheOfSolidarity.class, TrialOfSolidarity.class, MagmaSpray.class, Forest.class})
class BenefactionOfRhonasTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only the creature cards among the top five")
    void firstPickOffersCreatures() {
        setupTopFive(new Colossapede(), new MagmaSpray(), new CartoucheOfSolidarity(), new Forest(), new OashraCultivator());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Colossapede", "Oashra Cultivator");
    }

    @Test
    @DisplayName("Taking a creature and an enchantment puts both into hand; the rest go to the graveyard")
    void takesCreatureAndEnchantmentRestToGraveyard() {
        setupTopFive(new Colossapede(), new MagmaSpray(), new CartoucheOfSolidarity(), new Forest(), new OashraCultivator());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        // Pick the creature (Colossapede).
        chooseCard(gd, 0);

        // Second pick offers only the enchantment.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactly("Cartouche of Solidarity");

        // Pick the enchantment (Cartouche of Solidarity).
        chooseCard(gd, 0);

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getName))
                .containsExactlyInAnyOrder("Colossapede", "Cartouche of Solidarity");
        // The rest are binned, and no reorder interaction is opened.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Magma Spray", "Forest", "Oashra Cultivator");
    }

    @Test
    @DisplayName("Cannot take two creatures - the second pick is enchantment-only")
    void cannotTakeTwoCreatures() {
        setupTopFive(new Colossapede(), new CartoucheOfSolidarity(), new OashraCultivator(), new MagmaSpray(), new Forest());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, 0);

        assertThat(searchCards(gd)).doesNotContain("Oashra Cultivator").containsExactly("Cartouche of Solidarity");
    }

    @Test
    @DisplayName("Declining the creature still offers the enchantment pick")
    void decliningCreatureStillOffersEnchantment() {
        setupTopFive(new Colossapede(), new MagmaSpray(), new CartoucheOfSolidarity(), new Forest(), new OashraCultivator());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactly("Cartouche of Solidarity");
    }

    @Test
    @DisplayName("With no creatures, the enchantment pick begins directly")
    void noCreaturesGoesStraightToEnchantment() {
        setupTopFive(new CartoucheOfSolidarity(), new MagmaSpray(), new Forest(), new TrialOfSolidarity(), new Forest());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(searchCards(gd)).containsExactlyInAnyOrder("Cartouche of Solidarity", "Trial of Solidarity");
    }

    @Test
    @DisplayName("Declining both picks bins all five into the graveyard")
    void decliningBothBinsEverything() {
        setupTopFive(new Colossapede(), new CartoucheOfSolidarity(), new MagmaSpray(), new Forest(), new OashraCultivator());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        chooseCard(gd, -1); // decline the creature
        chooseCard(gd, -1); // decline the enchantment

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Colossapede", "Cartouche of Solidarity", "Magma Spray", "Forest", "Oashra Cultivator");
    }

    @Test
    @DisplayName("With no creatures or enchantments, all five are binned directly")
    void noEligibleBinsToGraveyardDirectly() {
        setupTopFive(new MagmaSpray(), new MagmaSpray(), new Forest(), new Forest(), new MagmaSpray());

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        // Five binned cards plus the resolved sorcery itself.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    void takingOnlyCreaturePutsEnchantmentIntoGraveyard() {
        Colossapede creature = new Colossapede();
        CartoucheOfSolidarity enchantment = new CartoucheOfSolidarity();
        setupTopFive(creature, enchantment, new Forest(), new Forest(), new MagmaSpray());

        resolveBenefaction();
        GameData gd = harness.getGameData();
        chooseCard(gd, 0);
        chooseCard(gd, -1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(enchantment).hasSize(5);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryAllowsTakingOnlyEnchantment() {
        Colossapede creature = new Colossapede();
        CartoucheOfSolidarity enchantment = new CartoucheOfSolidarity();
        setupTopFive(creature, enchantment);

        resolveBenefaction();
        GameData gd = harness.getGameData();
        chooseCard(gd, -1);
        chooseCard(gd, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void leavesCardsBelowTopFiveInOriginalOrder() {
        Colossapede sixth = new Colossapede();
        CartoucheOfSolidarity seventh = new CartoucheOfSolidarity();
        setupTopFive(new Forest(), new Forest(), new MagmaSpray(), new Forest(), new MagmaSpray(),
                sixth, seventh);

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sixth, seventh);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryFinishesWithoutChoice() {
        setupTopFive();

        resolveBenefaction();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void onlyCreatureFinishesWithoutEnchantmentChoice() {
        Colossapede creature = new Colossapede();
        setupTopFive(creature);

        resolveBenefaction();
        GameData gd = harness.getGameData();
        chooseCard(gd, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolveBenefaction() {
        harness.setHand(player1, List.of(new BenefactionOfRhonas()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private void chooseCard(GameData gd, int index) {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> searchCards(GameData gd) {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
