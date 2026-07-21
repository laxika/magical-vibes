package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ImminentDoomTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a doom counter")
    void entersWithDoomCounter() {
        harness.setHand(player1, List.of(new ImminentDoom()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        Permanent doom = findPermanent(player1, "Imminent Doom");
        assertThat(doom.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell with matching mana value deals that much damage and adds a doom counter")
    void matchingManaValueDealsDamageAndAddsCounter() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new ImminentDoom());
        doom.setCounterCount(CounterType.DOOM, 1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());

        // Choose target for Imminent Doom's trigger
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // resolve Imminent Doom trigger
        harness.passBothPriorities(); // resolve Shock

        harness.assertLife(player2, 17); // 1 from Doom + 2 from Shock
        assertThat(doom.getCounterCount(CounterType.DOOM)).isEqualTo(2);
    }

    @Test
    @DisplayName("Casting a spell with non-matching mana value does not trigger")
    void nonMatchingManaValueDoesNotTrigger() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new ImminentDoom());
        doom.setCounterCount(CounterType.DOOM, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        assertThat(doom.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is snapshotted at trigger time when a second matching spell is cast in response")
    void damageSnapshottedAtTriggerTime() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new ImminentDoom());
        doom.setCounterCount(CounterType.DOOM, 1);

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        // Respond to the trigger with another Shock while still at 1 doom counter
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        harness.passBothPriorities(); // first Doom trigger: 1 damage, put counter → 2
        harness.passBothPriorities(); // second Doom trigger: still 1 damage (snapshotted), put counter → 3
        harness.passBothPriorities(); // second Shock
        harness.passBothPriorities(); // first Shock

        // 1 + 1 from Doom triggers + 2 + 2 from Shocks
        harness.assertLife(player2, 14);
        assertThat(doom.getCounterCount(CounterType.DOOM)).isEqualTo(3);
    }

    @Test
    @DisplayName("Opponent casting a matching spell does not trigger")
    void opponentSpellDoesNotTrigger() {
        Permanent doom = harness.addToBattlefieldAndReturn(player1, new ImminentDoom());
        doom.setCounterCount(CounterType.DOOM, 1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        harness.passBothPriorities();

        harness.assertLife(player1, 18); // only Shock
        assertThat(doom.getCounterCount(CounterType.DOOM)).isEqualTo(1);
    }
}
