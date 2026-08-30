package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ZurTheEnchanterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking creates a may search prompt")
    void attackingCreatesMaySearchPrompt() {
        addReadyZur();
        harness.setLibrary(player1, List.of(enchantment("Low-Cost Enchantment", "{3}")));

        declareAttack();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the attack trigger puts an eligible enchantment onto the battlefield")
    void acceptingSearchPutsEnchantmentOntoBattlefield() {
        addReadyZur();
        Card enchantment = enchantment("Low-Cost Enchantment", "{3}");
        harness.setLibrary(player1, List.of(enchantment));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(
                gameData, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertOnBattlefield(player1, "Low-Cost Enchantment");
        assertThat(gameData.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the attack trigger does not search")
    void decliningSearchDoesNothing() {
        addReadyZur();
        harness.setLibrary(player1, List.of(enchantment("Low-Cost Enchantment", "{3}")));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Low-Cost Enchantment"));
    }

    @Test
    @DisplayName("The search offers only enchantments with mana value 3 or less")
    void searchFiltersByTypeAndManaValue() {
        addReadyZur();
        harness.setLibrary(player1, List.of(
                enchantment("Eligible Enchantment", "{3}"),
                enchantment("Too Expensive Enchantment", "{4}"),
                creature("Creature Card", "{1}")));

        declareAttack();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards())
                .extracting(Card::getName)
                .containsExactly("Eligible Enchantment");
    }

    private void addReadyZur() {
        addCreatureReady(player1, new ZurTheEnchanter());
    }

    private void declareAttack() {
        declareAttackers(player1, List.of(0));
    }

    private Card enchantment(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ENCHANTMENT);
        card.setManaCost(manaCost);
        return card;
    }

    private Card creature(String name, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        return card;
    }
}
