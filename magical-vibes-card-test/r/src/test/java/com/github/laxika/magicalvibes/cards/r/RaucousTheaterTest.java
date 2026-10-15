package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RaucousTheater.class})
class RaucousTheaterTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new RaucousTheater();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new RaucousTheater()));

        harness.playLand(player1, 0);
        Permanent theater = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(theater.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        Permanent theater = addReadyTheater();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(theater.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent theater = addReadyTheater();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(theater.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    private Permanent addReadyTheater() {
        Permanent theater = harness.addToBattlefieldAndReturn(player1, new RaucousTheater());
        theater.setSummoningSick(false);
        return theater;
    }

    @Test
    @DisplayName("Surveil may leave the top card in place without changing library order")
    void surveilMayLeaveTopCardInPlace() {
        Card topCard = new RaucousTheater();
        Card secondCard = new RaucousTheater();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new RaucousTheater()));

        harness.playLand(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library resolves without a choice")
    void surveilWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new RaucousTheater()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Surveil puts only the top card into its controller's graveyard")
    void surveilMovesOnlyControllersTopCard() {
        Card topCard = new RaucousTheater();
        Card secondCard = new RaucousTheater();
        Card opponentsTopCard = new RaucousTheater();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentsTopCard));
        harness.setHand(player1, List.of(new RaucousTheater()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsTopCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
