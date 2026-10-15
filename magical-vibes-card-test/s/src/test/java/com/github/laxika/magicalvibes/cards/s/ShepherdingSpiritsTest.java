package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalustradeWurm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShepherdingSpirits.class, Plains.class, Forest.class, BalustradeWurm.class})
class ShepherdingSpiritsTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling discards the card and offers only Plains cards")
    void plainscyclingDiscardsAndSearchesForPlains() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new BalustradeWurm()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shepherding Spirits");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.getName().equals("Plains"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Plainscycling puts the chosen Plains into hand")
    void plainscyclingChoosesPlains() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Plainscycling pays mana and discards before its search resolves")
    void paysCostsBeforeResolution() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.setLibrary(player1, List.of(new Plains()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Shepherding Spirits");
        harness.assertNotInHand(player1, "Shepherding Spirits");
        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling may fail to find even with a Plains available")
    void mayFailToFind() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Shepherding Spirits");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling resolves without drawing from an empty library")
    void resolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Shepherding Spirits");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling cannot discard its source without enough mana")
    void requiresTwoMana() {
        harness.setHand(player1, List.of(new ShepherdingSpirits()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shepherding Spirits");
        harness.assertNotInGraveyard(player1, "Shepherding Spirits");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
