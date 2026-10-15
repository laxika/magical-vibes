package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarshDeceiver.class, Forest.class})
class HarshDeceiverTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability looks at the top card and leaves it on top")
    void looksAtTopCard() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        Card topCard = new HarshDeceiver();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(topCard);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(deceiver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Revealing a land untaps Harsh Deceiver and gives it +1/+1")
    void landRevealUntapsAndBoosts() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        deceiver.tap();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(Forest.class);
    }

    @Test
    @DisplayName("Revealing a nonland card does not untap or boost Harsh Deceiver")
    void nonlandRevealDoesNothing() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        deceiver.tap();
        harness.setLibrary(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isInstanceOf(HarshDeceiver.class);
    }

    @Test
    @DisplayName("The reveal ability can only be activated once each turn")
    void revealAbilityOnlyOnceEachTurn() {
        addCreatureReady(player1, new HarshDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The look ability can be activated repeatedly in the same turn")
    void lookAbilityHasNoTurnLimit() {
        addCreatureReady(player1, new HarshDeceiver());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.WHITE, 2);

        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                    .params().cards()).containsExactly(topCard);
            harness.handleCardChosen(player1, -1);
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Looking at an empty library resolves without a choice or moving cards")
    void lookAtEmptyLibrary() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(deceiver.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The reveal limit applies before the first activation resolves")
    void cannotActivateRevealTwiceOnStack() {
        addCreatureReady(player1, new HarshDeceiver());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("An empty library gives no untap or boost and still uses the reveal activation")
    void emptyLibraryRevealUsesActivation() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        deceiver.tap();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The reveal ability checks the top card when it resolves")
    void revealChecksLibraryAtResolution() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        deceiver.tap();
        harness.setLibrary(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(5);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    @DisplayName("An untapped Harsh Deceiver still gets the land bonus, which expires at end of turn")
    void bonusExpiresAndAbilityResetsOnOpponentsTurn() {
        Permanent deceiver = addCreatureReady(player1, new HarshDeceiver());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(4);
        deceiver.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(deceiver.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, deceiver)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, deceiver)).isEqualTo(5);
    }
}
