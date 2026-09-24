package com.github.laxika.magicalvibes.cards.z;

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

@CardUsed({ZimonesHypothesis.class, GrizzlyBears.class, LlanowarElves.class})
class ZimonesHypothesisTest extends BaseCardTest {

    private void castHypothesis() {
        harness.setHand(player1, List.of(new ZimonesHypothesis()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The counter is optional and the spell then asks for odd or even")
    void counterIsOptional() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());

        castHypothesis();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "EVEN");

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(elf);
    }

    @Test
    @DisplayName("The chosen creature may be controlled by an opponent and its new power is used")
    void counterChangesPowerParityBeforeBounce() {
        Permanent ownOdd = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentEven = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castHypothesis();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultiplePermanentsChosen(player1, List.of(opponentEven.getId()));
        harness.handleListChoice(player1, "ODD");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownOdd);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentEven);
        assertThat(gd.playerHands.get(player1.getId()).stream().map(card -> card.getName()))
                .contains("Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId()).stream().map(card -> card.getName()))
                .contains("Grizzly Bears");
    }
}
