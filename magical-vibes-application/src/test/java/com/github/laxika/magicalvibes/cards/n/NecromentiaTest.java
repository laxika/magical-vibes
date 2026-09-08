package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NecromentiaTest extends BaseCardTest {

    private void addManaAndCast() {
        harness.setHand(player1, List.of(new Necromentia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new Necromentia()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Exiles selected copies and gives the opponent Zombies for hand copies")
    void exilesCopiesAndCreatesZombiesForHandCopies() {
        Card handBears1 = new GrizzlyBears();
        Card handBears2 = new GrizzlyBears();
        Card graveyardBears = new GrizzlyBears();
        Card libraryBears = new GrizzlyBears();

        harness.setHand(player2, new ArrayList<>(List.of(handBears1, handBears2, new Plains())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardBears)));
        harness.setLibrary(player2, List.of(libraryBears));

        addManaAndCast();

        PendingInteraction.ColorChoice nameChoice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(nameChoice.options()).contains("Grizzly Bears").doesNotContain("Plains");
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1,
                List.of(handBears1.getId(), handBears2.getId(), graveyardBears.getId(), libraryBears.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(4);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"))
                .hasSize(2)
                .allMatch(p -> p.getEffectivePower() == 2 && p.getEffectiveToughness() == 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"));
    }

    @Test
    @DisplayName("Selecting no hand copies creates no Zombies")
    void noHandCopiesCreateNoZombies() {
        Card handBears = new GrizzlyBears();
        Card graveyardBears = new GrizzlyBears();
        harness.setHand(player2, new ArrayList<>(List.of(handBears)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(graveyardBears)));

        addManaAndCast();
        harness.handleListChoice(player1, "Grizzly Bears");
        harness.handleMultipleCardsChosen(player1, List.of(graveyardBears.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken() && p.getCard().getName().equals("Zombie"));
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
