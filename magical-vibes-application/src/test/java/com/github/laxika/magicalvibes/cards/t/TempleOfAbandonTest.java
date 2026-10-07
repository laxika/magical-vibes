package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TempleOfAbandon.class})
class TempleOfAbandonTest extends BaseCardTest {

    @Test
    void entersTappedAndTriggersScry() {
        playTempleOfAbandon();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
    }

    @Test
    void scryCanPutTopCardOnBottom() {
        playTempleOfAbandon();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.getFirst();

        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.getLast()).isSameAs(originalTop);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void producesRedMana() {
        addCreatureReady(player1, new TempleOfAbandon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void producesGreenMana() {
        addCreatureReady(player1, new TempleOfAbandon());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    void manaAbilityResolvesImmediatelyAndCannotProduceBothColorsWithOneTap() {
        harness.addToBattlefield(player1, new TempleOfAbandon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void playTempleOfAbandon() {
        harness.setHand(player1, List.of(new TempleOfAbandon()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    @Test
    void scryCanKeepTopCardWithoutChangingLibraryOrder() {
        Card top = new TempleOfAbandon();
        Card second = new TempleOfAbandon();
        harness.setLibrary(player1, List.of(top, second));
        playTempleOfAbandon();

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryWithEmptyLibraryCompletesWithoutInput() {
        harness.setLibrary(player1, List.of());
        playTempleOfAbandon();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void scryStillResolvesAfterTempleLeavesBattlefield() {
        Card top = new TempleOfAbandon();
        Card second = new TempleOfAbandon();
        harness.setLibrary(player1, List.of(top, second));
        playTempleOfAbandon();
        Card temple = gd.playerBattlefields.get(player1.getId()).removeFirst().getCard();
        harness.setGraveyard(player1, List.of(temple));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, top);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
