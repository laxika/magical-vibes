package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
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

class DesynchronizeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target on top, then scries 2")
    void putsTargetOnTopThenScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card scryTop = new Island();
        Card scryBottom = new Island();
        setDeck(player2, List.of(targetTop, targetBottom));
        setDeck(player1, List.of(scryTop, scryBottom));

        castDesynchronize(target);
        harness.handleListChoice(player2, "Top");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), targetTop, targetBottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryTop, scryBottom);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Desynchronize");
    }

    @Test
    @DisplayName("Puts the target on the bottom, then scries 2")
    void putsTargetOnBottomThenScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card scryTop = new Island();
        Card scryBottom = new Island();
        setDeck(player2, List.of(targetTop, targetBottom));
        setDeck(player1, List.of(scryTop, scryBottom));

        castDesynchronize(target);
        harness.handleListChoice(player2, "Bottom");
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetTop, targetBottom, target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(scryBottom, scryTop);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Desynchronize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castDesynchronize(Permanent target) {
        harness.setHand(player1, List.of(new Desynchronize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        harness.setLibrary(player, cards);
    }
}
