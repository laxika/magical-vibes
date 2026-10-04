package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElegantParlor.class})
class ElegantParlorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new ElegantParlor();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ElegantParlor()));

        harness.playLand(player1, 0);
        Permanent parlor = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(parlor.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil may leave the top card in place without changing either library")
    void surveilCanKeepTopCard() {
        Card topCard = new ElegantParlor();
        Card secondCard = new ElegantParlor();
        Card opponentCard = new ElegantParlor();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new ElegantParlor()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil with an empty library finishes without a choice")
    void surveilWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ElegantParlor()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The surveil trigger resolves after the land leaves the battlefield")
    void surveilResolvesWithoutItsSource() {
        Card topCard = new ElegantParlor();
        Card secondCard = new ElegantParlor();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new ElegantParlor()));

        harness.playLand(player1, 0);
        assertThat(gd.stack).hasSize(1);
        Permanent parlor = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerHands.get(player1.getId()).add(parlor.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        tapFor(ManaColor.RED);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    private void tapFor(ManaColor color) {
        Permanent parlor = harness.addToBattlefieldAndReturn(player1, new ElegantParlor());

        harness.activateAbility(player1, 0, color == ManaColor.RED ? 0 : 1, null, null);

        assertThat(parlor.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

}
