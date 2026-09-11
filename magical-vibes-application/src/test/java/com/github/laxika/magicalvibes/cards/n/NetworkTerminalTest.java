package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NetworkTerminal.class, DarksteelRelic.class, Forest.class})
class NetworkTerminalTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Network Terminal adds one mana of a chosen color")
    void tapsForChosenColor() {
        Permanent terminal = addReady(player1, new NetworkTerminal());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(terminal.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapping another artifact lets Network Terminal draw, then discard")
    void tapsAnotherArtifactAndLoots() {
        Permanent terminal = addReady(player1, new NetworkTerminal());
        Permanent relic = addReady(player1, new DarksteelRelic());
        Card keptCard = new Forest();
        Card drawnCard = new Forest();
        harness.setHand(player1, List.of(keptCard));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(terminal.isTapped()).isTrue();
        assertThat(relic.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(keptCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot tap Network Terminal itself as the other artifact")
    void requiresAnotherUntappedArtifact() {
        Permanent terminal = addReady(player1, new NetworkTerminal());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot use a tapped artifact to pay the ability's artifact cost")
    void requiresUntappedArtifact() {
        Permanent terminal = addReady(player1, new NetworkTerminal());
        Permanent relic = addReady(player1, new DarksteelRelic());
        relic.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(terminal.isTapped()).isFalse();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
