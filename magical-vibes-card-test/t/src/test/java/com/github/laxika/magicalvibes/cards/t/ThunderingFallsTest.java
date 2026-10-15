package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ThunderingFalls.class, GrizzlyBears.class})
class ThunderingFallsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ThunderingFalls()));

        harness.playLand(player1, 0);
        Permanent falls = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(falls.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for blue mana")
    void tapsForBlueMana() {
        Permanent falls = addReadyFalls();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(falls.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent falls = addReadyFalls();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(falls.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepTopCard() {
        Card topCard = new ThunderingFalls();
        Card nextCard = new ThunderingFalls();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new ThunderingFalls()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil with an empty library finishes without a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ThunderingFalls()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Thundering Falls").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Entering without being played still enters tapped and surveils only the top card")
    void entryWithoutPlayingSurveilsOne() {
        Card topCard = new ThunderingFalls();
        Card nextCard = new ThunderingFalls();
        Card opponentTopCard = new ThunderingFalls();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setLibrary(player2, List.of(opponentTopCard));

        Permanent falls = harness.enterBattlefieldAndReturn(player1, new ThunderingFalls());
        assertThat(falls.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    private Permanent addReadyFalls() {
        return addCreatureReady(player1, new ThunderingFalls());
    }
}
