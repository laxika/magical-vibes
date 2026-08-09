package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonsHoardTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a gold counter on itself when a Dragon enters under your control")
    void dragonEntryAddsGoldCounter() {
        Permanent hoard = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(hoard.getCounterCount(CounterType.GOLD)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for a non-Dragon creature")
    void nonDragonEntryDoesNotAddGoldCounter() {
        Permanent hoard = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(hoard.getCounterCount(CounterType.GOLD)).isZero();
    }

    @Test
    @DisplayName("Removing a gold counter draws a card")
    void removingGoldCounterDrawsCard() {
        Permanent hoard = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());
        hoard.setCounterCount(CounterType.GOLD, 1);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(hoard.getCounterCount(CounterType.GOLD)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(hoard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot draw without a gold counter")
    void drawAbilityRequiresGoldCounter() {
        Permanent hoard = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());
        hoard.setCounterCount(CounterType.GOLD, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent hoard = harness.addToBattlefieldAndReturn(player1, new DragonsHoard());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(hoard.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
