package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathToTheWorldTreeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters and searches the library for a basic land")
    void entersAndSearchesForBasicLand() {
        harness.setHand(player1, List.of(new PathToTheWorldTree()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        setLibrary(new GrizzlyBears(), new Forest());

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getName)
                .containsExactly("Forest");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Activated ability pays all effects and damages its optional creature target")
    void activatedAbilityResolvesWithCreatureTarget() {
        harness.addToBattlefield(player1, new PathToTheWorldTree());
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        setLibrary(new Forest(), new Forest());
        addAbilityMana();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Path to the World Tree");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Bear");
    }

    @Test
    @DisplayName("Activated ability resolves without choosing its optional creature target")
    void activatedAbilityResolvesWithoutCreatureTarget() {
        harness.addToBattlefield(player1, new PathToTheWorldTree());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setLibrary(new Forest(), new Forest());
        addAbilityMana();
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Path to the World Tree");
        harness.assertOnBattlefield(player1, "Bear");
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private void setLibrary(Card... cards) {
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(cards));
    }
}
