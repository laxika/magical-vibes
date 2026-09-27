package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(VedalkenEntrancer.class)
class VedalkenEntrancerTest extends BaseCardTest {

    @Test
    @DisplayName("Target player mills two cards")
    void millsTwoCards() {
        addCreatureReady(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();
        Card first = deck.get(0);
        Card second = deck.get(1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Can target yourself with the mill ability")
    void canTargetSelf() {
        addCreatureReady(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        while (deck.size() > 5) {
            deck.removeFirst();
        }
        int deckSizeBefore = deck.size();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Mills only the remaining card when the library has one card")
    void millsPartialLibrary() {
        addCreatureReady(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> deck = gd.playerDecks.get(player2.getId());
        while (deck.size() > 1) {
            deck.removeFirst();
        }
        Card last = deck.getFirst();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(last);
    }

    @Test
    @DisplayName("Cannot activate the ability without blue mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new VedalkenEntrancer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating the ability taps Vedalken Entrancer")
    void activatingTapsEntrancer() {
        Permanent entrancer = addCreatureReady(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(entrancer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability while Vedalken Entrancer is tapped")
    void cannotActivateWhenTapped() {
        Permanent entrancer = addCreatureReady(player1, new VedalkenEntrancer());
        entrancer.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot target a permanent with the mill ability")
    void cannotTargetPermanent() {
        Permanent entrancer = addCreatureReady(player1, new VedalkenEntrancer());
        Permanent invalidTarget = harness.addToBattlefieldAndReturn(player2, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, invalidTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");

        assertThat(entrancer.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mills nothing when the target player's library is empty")
    void millsNothingWhenLibraryEmpty() {
        addCreatureReady(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);
        gd.playerDecks.get(player2.getId()).clear();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new VedalkenEntrancer());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

}
