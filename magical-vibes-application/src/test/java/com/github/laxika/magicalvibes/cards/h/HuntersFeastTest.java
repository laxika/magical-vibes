package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.RainOfGore;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HuntersFeast.class, RainOfGore.class})
class HuntersFeastTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting targeting both players puts spell on the stack")
    void castingTargetingBothPlayersPutsOnStack() {
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetIds()).containsExactly(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Both targeted players each gain 6 life")
    void bothPlayersGain6Life() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 12);
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 16);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Targeting only one player gains 6 life for that player only")
    void targetingOnePlayerGainsLifeForThatPlayerOnly() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player2.getId()));

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 16);
    }

    @Test
    @DisplayName("Can be cast with zero targets (resolves doing nothing)")
    void canBeCastWithZeroTargets() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of());

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Goes to graveyard after resolution")
    void goesToGraveyardAfterResolution() {
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId()));

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Hunters' Feast");
    }
    @Test
    @DisplayName("Cannot choose the same player twice")
    void cannotTargetSamePlayerTwice() {
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rain of Gore replaces the caster's gain but not the opponent's gain")
    void rainOfGoreReplacesOnlySpellControllersLifeGain() {
        harness.addToBattlefield(player2, new RainOfGore());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HuntersFeast()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 14);
        harness.assertLife(player2, 26);
    }
}
