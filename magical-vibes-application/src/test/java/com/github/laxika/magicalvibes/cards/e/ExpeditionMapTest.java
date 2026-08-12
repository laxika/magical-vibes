package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExpeditionMapTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Expedition Map sacrifices it")
    void activatingSacrificesSelf() {
        addMapAndMana();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Expedition Map");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving presents all land cards for the search")
    void resolvingPresentsLandCards() {
        addMapAndMana();
        setLibrary(new Forest(), new Island(), new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Choosing a land puts it into hand")
    void chosenLandEntersHand() {
        addMapAndMana();
        setLibrary(new Forest(), new Island(), new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a land")
    void searchMayFailToFind() {
        addMapAndMana();
        setLibrary(new GrizzlyBears(), new GrizzlyBears());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Cannot activate Expedition Map without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new ExpeditionMap());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addMapAndMana() {
        harness.addToBattlefield(player1, new ExpeditionMap());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
