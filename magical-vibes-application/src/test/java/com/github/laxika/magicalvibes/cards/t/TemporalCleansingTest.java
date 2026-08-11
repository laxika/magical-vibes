package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemporalCleansingTest extends BaseCardTest {

    @Test
    @DisplayName("The target's owner can keep it second from the top")
    void targetOwnerChoosesSecondFromTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        setDeck(player2, List.of(topCard, bottomCard));

        castTemporalCleansing(target);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.TargetLibraryDestinationChoice.class)
                .options()).containsExactly("Second from the top", "Bottom");

        harness.handleListChoice(player2, "Second from the top");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, target.getCard(), bottomCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Temporal Cleansing");
    }

    @Test
    @DisplayName("The target's owner can put it on the bottom")
    void targetOwnerChoosesBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        Card bottomCard = new Island();
        setDeck(player2, List.of(topCard, bottomCard));

        castTemporalCleansing(target);
        harness.handleListChoice(player2, "Bottom");

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(topCard, bottomCard, target.getCard());
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new TemporalCleansing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTemporalCleansing(Permanent target) {
        harness.setHand(player1, List.of(new TemporalCleansing()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
