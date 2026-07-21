package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectralRider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NebelgastHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature an opponent controls")
    void selfEntryTapsOpponentCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHerald(player1, victim.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(victim.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Nebelgast Herald");
    }

    @Test
    @DisplayName("Another Spirit entering queues a tap trigger for target selection")
    void anotherSpiritEnterQueuesTargetSelection() {
        harness.addToBattlefield(player1, new NebelgastHerald());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castSpectralRider(player1);
        harness.passBothPriorities(); // Spirit enters → Herald triggers

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.EntersTriggerTarget.class);

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A non-Spirit creature entering does not trigger the tap ability")
    void nonSpiritEnterDoesNotTrigger() {
        harness.addToBattlefield(player1, new NebelgastHerald());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target own creature on cast")
    void cannotTargetOwnCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NebelgastHerald()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, own.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("ETB trigger goes on the stack when Nebelgast Herald enters")
    void etbTriggerGoesOnStack() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castHerald(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nebelgast Herald");
    }

    private void castHerald(Player player, UUID targetId) {
        harness.setHand(player, List.of(new NebelgastHerald()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, 0, targetId);
    }

    private void castSpectralRider(Player player) {
        harness.setHand(player, List.of(new SpectralRider()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castCreature(player, 0);
    }
}
