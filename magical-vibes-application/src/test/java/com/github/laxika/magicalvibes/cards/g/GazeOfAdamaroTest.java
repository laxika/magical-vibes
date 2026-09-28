package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GazeOfAdamaro.class, ArabaMothrider.class})
class GazeOfAdamaroTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Gaze of Adamaro targeting a player puts it on the stack")
    void castingTargetingPlayerPutsItOnStack() {
        harness.setHand(player1, List.of(new GazeOfAdamaro()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Gaze of Adamaro deals damage equal to target player's hand size")
    void dealsDamageEqualToHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GazeOfAdamaro()));
        harness.setHand(player2, List.of(
                new ArabaMothrider(), new ArabaMothrider(), new ArabaMothrider()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gaze of Adamaro uses target player's hand size on resolution")
    void usesHandSizeOnResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GazeOfAdamaro()));
        harness.setHand(player2, List.of(new ArabaMothrider(), new ArabaMothrider()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        gd.playerHands.get(player2.getId()).add(new ArabaMothrider());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Gaze of Adamaro deals 0 damage if target player has no cards in hand")
    void dealsZeroDamageWithEmptyHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new GazeOfAdamaro()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Gaze of Adamaro cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArabaMothrider());

        harness.setHand(player1, List.of(new GazeOfAdamaro()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
