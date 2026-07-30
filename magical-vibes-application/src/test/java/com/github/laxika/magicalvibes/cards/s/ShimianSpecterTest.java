package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ShimianSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage prompts the controller to pick a nonland card from the revealed hand")
    void combatDamagePromptsChoice() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Peek(), createForest())));

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        // Lands are excluded and only cards actually in the revealed hand are choosable.
        assertThat(choice.options()).containsExactlyInAnyOrder("Grizzly Bears", "Peek");
    }

    @Test
    @DisplayName("Exiles every copy of the chosen card from hand, graveyard, and library")
    void exilesAllCopiesFromAllZones() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears(), new Peek())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(new GrizzlyBears());

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Grizzly Bears");

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).count();
        assertThat(exiled).isEqualTo(4);
        harness.assertNotInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Cards with a different name are untouched.
        harness.assertInHand(player2, "Peek");
    }

    @Test
    @DisplayName("Deals no damage beyond combat damage — only the exile happens")
    void dealsNoExtraDamage() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new GrizzlyBears())));

        int lifeAfterCombat;
        resolveCombatAndTrigger();
        lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());
        harness.handleListChoice(player1, "Grizzly Bears");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat);
    }

    @Test
    @DisplayName("No prompt when the revealed hand holds only lands")
    void noPromptWhenHandIsAllLands() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(createForest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("No prompt when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addAttackingSpecter(Player player) {
        Permanent specter = new Permanent(new ShimianSpecter());
        specter.setSummoningSick(false);
        specter.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(specter);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }

    private Card createForest() {
        Card card = new Card();
        card.setName("Forest");
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.FOREST));
        return card;
    }
}
