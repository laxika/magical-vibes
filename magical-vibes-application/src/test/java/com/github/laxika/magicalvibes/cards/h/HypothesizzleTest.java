package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HypothesizzleTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discarding a nonland card deals 4 damage to a creature")
    void drawsAndDealsDamageAfterDiscarding() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        int nonlandIndex = findCardIndex(player1, "Grizzly Bears");
        harness.handleCardChosen(player1, nonlandIndex);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the discard still keeps the two drawn cards and deals no damage")
    void decliningDiscardDealsNoDamage() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player2, new GrizzlyBears());
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The discard choice offers nonland cards but not lands")
    void onlyOffersNonlandCards() {
        harness.setHand(player1, List.of(new Hypothesizzle()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.DiscardChoice choice = (PendingInteraction.DiscardChoice) gd.interaction.activeInteraction();
        int landIndex = findCardIndex(player1, "Forest");
        int nonlandIndex = findCardIndex(player1, "Grizzly Bears");
        assertThat(choice.validIndices()).contains(nonlandIndex).doesNotContain(landIndex);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private int findCardIndex(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        return java.util.stream.IntStream.range(0, gd.playerHands.get(player.getId()).size())
                .filter(i -> gd.playerHands.get(player.getId()).get(i).getName().equals(cardName))
                .findFirst()
                .orElseThrow();
    }
}
