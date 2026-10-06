package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
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

@CardUsed({ShadowyBackstreet.class, NoviceInspector.class})
class ShadowyBackstreetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and surveils 1")
    void entersTappedAndSurveilsOne() {
        Card topCard = new NoviceInspector();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ShadowyBackstreet()));

        harness.playLand(player1, 0);
        Permanent backstreet = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(backstreet.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        tapFor(ManaColor.WHITE);
    }

    @Test
    @DisplayName("Taps for black mana")
    void tapsForBlackMana() {
        tapFor(ManaColor.BLACK);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void mayKeepTopCard() {
        Card topCard = new NoviceInspector();
        Card secondCard = new ShadowyBackstreet();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new ShadowyBackstreet()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Surveil only puts the top card into its controller's graveyard")
    void surveilsOnlyControllersTopCard() {
        Card topCard = new NoviceInspector();
        Card secondCard = new ShadowyBackstreet();
        Card opponentsCard = new NoviceInspector();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setLibrary(player2, List.of(opponentsCard));
        harness.setHand(player1, List.of(new ShadowyBackstreet()));

        harness.playLand(player1, 0);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Surveil with an empty library completes without a choice")
    void surveilsEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ShadowyBackstreet()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    private void tapFor(ManaColor color) {
        Permanent backstreet = harness.addToBattlefieldAndReturn(player1, new ShadowyBackstreet());

        harness.activateAbility(player1, 0, color == ManaColor.WHITE ? 0 : 1, null, null);

        assertThat(backstreet.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
    }

}
