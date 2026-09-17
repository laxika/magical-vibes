package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RonaSheoldredsFaithful.class, GrizzlyBears.class, Shock.class})
class RonaSheoldredsFaithfulTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you cast an instant, each opponent loses 1 life")
    void instantCastMakesEachOpponentLoseLife() {
        harness.addToBattlefield(player1, new RonaSheoldredsFaithful());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A creature spell does not trigger Rona")
    void creatureCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new RonaSheoldredsFaithful());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Rona can be cast from the graveyard by discarding two cards")
    void castsFromGraveyardByDiscardingTwoCards() {
        harness.setGraveyard(player1, List.of(new RonaSheoldredsFaithful()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addRonaMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rona, Sheoldred's Faithful");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                .map(Card::getName)
                .filter("Grizzly Bears"::equals)
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Rona cannot be cast from the graveyard without two cards to discard")
    void cannotCastFromGraveyardWithoutTwoDiscardCards() {
        harness.setGraveyard(player1, List.of(new RonaSheoldredsFaithful()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addRonaMana();

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard exactly 2 cards");
        harness.assertInGraveyard(player1, "Rona, Sheoldred's Faithful");
    }

    private void addRonaMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
    }
}
