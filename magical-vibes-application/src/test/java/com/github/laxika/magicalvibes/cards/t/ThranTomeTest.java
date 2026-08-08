package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThranTomeTest extends BaseCardTest {

    @Test
    @DisplayName("The targeted opponent chooses a revealed card to graveyard, then the controller draws two")
    void opponentChoosesCardThenControllerDrawsTwo() {
        Card top = new Island();
        Card chosen = new Forest();
        Card third = new Mountain();
        Card fourth = new Island();
        setDeck(player1, top, chosen, third, fourth);
        harness.setHand(player1, List.of());
        addReadyThranTome(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch choice =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(choice).isNotNull();
        assertThat(choice.params().playerId()).isEqualTo(player2.getId());
        assertThat(choice.params().targetPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.params().cards()).extracting(Card::getId)
                .containsExactly(top.getId(), chosen.getId(), third.getId());

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(chosen.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(top.getId(), third.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(fourth.getId());
    }

    @Test
    @DisplayName("With fewer than three cards, the opponent chooses from the available cards")
    void usesAvailableCardsWhenLibraryHasFewerThanThree() {
        Card top = new Island();
        Card second = new Forest();
        setDeck(player1, top, second);
        harness.setHand(player1, List.of());
        addReadyThranTome(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(top.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(second.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        setDeck(player1, new Island(), new Forest(), new Mountain());
        addReadyThranTome(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, Card... cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(List.of(cards));
    }

    private Permanent addReadyThranTome(Player player) {
        Permanent permanent = new Permanent(new ThranTome());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
