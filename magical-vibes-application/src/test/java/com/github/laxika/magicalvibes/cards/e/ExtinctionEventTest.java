package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExtinctionEvent.class, GrizzlyBears.class, LlanowarElves.class})
class ExtinctionEventTest extends BaseCardTest {

    private void castExtinctionEvent() {
        harness.setHand(player1, List.of(new ExtinctionEvent()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Resolution asks the controller to choose odd or even")
    void asksForParityAtResolution() {
        castExtinctionEvent();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("ODD", "EVEN");
    }

    @Test
    @DisplayName("Exiles matching creatures on every battlefield and leaves other creatures")
    void exilesCreaturesWithChosenParity() {
        Permanent oddOwn = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent evenOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent oddOpponent = harness.addToBattlefieldAndReturn(player2, new LlanowarElves());
        Permanent evenOpponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castExtinctionEvent();
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(evenOwn)
                .doesNotContain(oddOwn);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(evenOpponent)
                .doesNotContain(oddOpponent);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(oddOwn.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(oddOpponent.getCard());
    }

    @Test
    @DisplayName("Choosing even exiles creatures with even mana values")
    void exilesEvenManaValueCreatures() {
        Permanent oddOwn = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent evenOwn = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castExtinctionEvent();
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(oddOwn)
                .doesNotContain(evenOwn);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(evenOwn.getCard());
    }
}
