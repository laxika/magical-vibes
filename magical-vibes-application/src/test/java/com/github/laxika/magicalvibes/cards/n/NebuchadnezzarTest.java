package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NebuchadnezzarTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses a name and discards every matching card among the revealed hand")
    void discardsMatchingRevealedCards() {
        addReadyNebuchadnezzar(player1);
        Card firstNamed = named("Chosen Card");
        Card secondNamed = named("Chosen Card");
        Card other = named("Other Card");
        harness.setHand(player2, List.of(firstNamed, secondNamed, other));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 4, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Chosen Card");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyInAnyOrder(firstNamed, secondNamed);
    }

    @Test
    @DisplayName("X of zero reveals no cards and discards nothing")
    void zeroRevealsNoCards() {
        addReadyNebuchadnezzar(player1);
        Card namedCard = named("Chosen Card");
        harness.setHand(player2, List.of(namedCard));

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Chosen Card");

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(namedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target only an opponent")
    void cannotTargetController() {
        addReadyNebuchadnezzar(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only during its controller's turn")
    void onlyDuringYourTurn() {
        addReadyNebuchadnezzar(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    private Permanent addReadyNebuchadnezzar(Player player) {
        Permanent permanent = new Permanent(new Nebuchadnezzar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private static Card named(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.BLUE);
        return card;
    }
}
