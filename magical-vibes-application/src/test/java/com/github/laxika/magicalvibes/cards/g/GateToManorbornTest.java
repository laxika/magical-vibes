package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Owlbear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GateToManorborn.class, GrizzlyBears.class, Forest.class, Owlbear.class})
class GateToManorbornTest extends BaseCardTest {

    @Test
    @DisplayName("The gate enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GateToManorborn()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gate to Manorborn").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The gate produces green mana")
    void producesGreenMana() {
        Permanent gate = addReadyGate();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The gate seeks a nonland card once")
    void seeksNonlandCardOnce() {
        Permanent gate = addReadyGate();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest, bears));
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    @Test
    @DisplayName("The seek ability cannot be activated twice")
    void cannotSeekTwice() {
        Permanent gate = addReadyGate();
        harness.setLibrary(player1, List.of());
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gate.untap();
        addSeekMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("Seeking with only lands leaves the library unchanged")
    void noMatchingCardLeavesLibraryUnchanged() {
        addReadyGate();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second));
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("The once-only restriction applies before the first activation resolves")
    void cannotActivateAgainWhileSeekIsOnStack() {
        Permanent gate = addReadyGate();
        harness.setLibrary(player1, List.of());
        addSeekMana();
        harness.activateAbility(player1, 0, 1, null, null);
        gate.untap();
        addSeekMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The mana ability remains available after seeking")
    void canProduceManaAfterSeeking() {
        Permanent gate = addReadyGate();
        harness.setLibrary(player1, List.of());
        addSeekMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gate.untap();
        int greenBefore = gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN))
                .isEqualTo(greenBefore + 1);
    }

    @Test
    @DisplayName("Each gate has its own once-only activation allowance")
    void anotherGateCanSeekAfterFirstGateWasUsed() {
        addReadyGate();
        Permanent second = addReadyGate();
        harness.setLibrary(player1, List.of());
        addSeekMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        addSeekMana();

        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(second.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({GateToManorborn.class, Owlbear.class, Forest.class})
    @DisplayName("Seeking keeps the selected card private and preserves remaining library order")
    void seekDoesNotRevealSelectedCardToOpponent() {
        addReadyGate();
        Owlbear sought = new Owlbear();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, sought, second));
        addSeekMana();
        harness.clearMessages();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(harness.getConn2().getMessagesContaining("Owlbear")).isEmpty();
    }

    private Permanent addReadyGate() {
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new GateToManorborn());
        gate.setSummoningSick(false);
        return gate;
    }

    private void addSeekMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
