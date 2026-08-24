package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamishedForagers.class, GrizzlyBears.class, Shock.class})
class FamishedForagersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without adding mana when no opponent lost life this turn")
    void noManaWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new FamishedForagers()));
        harness.addMana(player1, ManaColor.RED, 4);
        forceMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Adds three red mana when an opponent lost life this turn")
    void addsManaAfterOpponentLifeLoss() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Shock(), new FamishedForagers()));
        harness.addMana(player1, ManaColor.RED, 5);
        forceMainPhase(player1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        forceMainPhase(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    @DisplayName("Discards a card to draw a card")
    void discardsAndDraws() {
        Permanent foragers = new Permanent(new FamishedForagers());
        foragers.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(foragers);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private void forceMainPhase(com.github.laxika.magicalvibes.model.Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
