package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavingVisionary.class, Forest.class, GrizzlyBears.class, Millstone.class, Shock.class})
class RavingVisionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Loots by drawing a card and then discarding a card")
    void loots() {
        Permanent visionary = addReadyVisionary();
        Card discarded = new Shock();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(visionary), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInHand(player1, "Forest");
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws with the delirium ability")
    void drawsWithDelirium() {
        Permanent visionary = addReadyVisionary();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(visionary), 1, null, null);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(visionary.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot use the delirium ability without four card types")
    void cannotDrawWithoutDelirium() {
        Permanent visionary = addReadyVisionary();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(visionary), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
    }

    @Test
    @DisplayName("Can discard the card just drawn instead of a card already in hand")
    void canDiscardDrawnCard() {
        Permanent visionary = addReadyVisionary();
        harness.setHand(player1, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, battlefieldIndex(visionary), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        harness.assertInHand(player1, "Shock");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Delirium is checked on activation and not again on resolution")
    void drawsAfterLosingDelirium() {
        Permanent visionary = addReadyVisionary();
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(visionary), 1, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Four cards with only three card types do not enable delirium")
    void duplicateTypesDoNotEnableDelirium() {
        Permanent visionary = addReadyVisionary();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new RavingVisionary(), new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(visionary), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
        assertThat(visionary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Card types in an opponent's graveyard do not enable delirium")
    void opponentsGraveyardDoesNotEnableDelirium() {
        Permanent visionary = addReadyVisionary();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));
        harness.setGraveyard(player2, List.of(new Millstone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(visionary), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
        assertThat(visionary.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyVisionary() {
        return addCreatureReady(player1, new RavingVisionary());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
