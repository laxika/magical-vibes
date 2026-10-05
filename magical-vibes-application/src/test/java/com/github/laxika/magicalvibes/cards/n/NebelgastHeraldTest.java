package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.s.StormriderSpirit;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NebelgastHerald.class, DevilthornFox.class, StormriderSpirit.class})
class NebelgastHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target creature an opponent controls")
    void selfEntryTapsOpponentCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
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
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        castStormriderSpirit(player1);
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
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        harness.setHand(player1, List.of(new DevilthornFox()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(victim.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Spirit's entry trigger cannot target a creature you control")
    void cannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new NebelgastHerald());
        Permanent own = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        castStormriderSpirit(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, own.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(own.isTapped()).isFalse();
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("ETB trigger goes on the stack when Nebelgast Herald enters")
    void etbTriggerGoesOnStack() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        castHerald(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Nebelgast Herald");
    }

    @Test
    @DisplayName("Herald can enter with no opposing creatures")
    void entersWithoutLegalTargets() {
        castHerald(player1, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nebelgast Herald");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Another Spirit can enter with no opposing creatures")
    void anotherSpiritEntersWithoutLegalTargets() {
        harness.addToBattlefield(player1, new NebelgastHerald());
        castStormriderSpirit(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Stormrider Spirit");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's Spirit does not trigger Herald")
    void opponentSpiritDoesNotTrigger() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new NebelgastHerald());
        harness.forceActivePlayer(player2);
        castStormriderSpirit(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Stormrider Spirit");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(herald.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flash allows Herald to enter during an opponent's turn")
    void flashDuringOpponentsTurn() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.forceActivePlayer(player2);
        castHerald(player1, victim.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nebelgast Herald");
        assertThat(victim.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A second Herald triggers itself and the Herald already on the battlefield")
    void secondHeraldTriggersBothHeralds() {
        harness.addToBattlefield(player1, new NebelgastHerald());
        Permanent firstVictim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        Permanent secondVictim = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        castHerald(player1, firstVictim.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, secondVictim.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstVictim.isTapped()).isTrue();
        assertThat(secondVictim.isTapped()).isTrue();
    }

    private void castHerald(Player player, UUID targetId) {
        harness.setHand(player, List.of(new NebelgastHerald()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castCreature(player, 0, 0, targetId);
    }

    private void castStormriderSpirit(Player player) {
        harness.setHand(player, List.of(new StormriderSpirit()));
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 4);
        harness.castCreature(player, 0);
    }
}
