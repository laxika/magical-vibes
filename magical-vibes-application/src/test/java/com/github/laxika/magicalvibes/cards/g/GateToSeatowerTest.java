package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.SteadfastPaladin;
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

@CardUsed({GateToSeatower.class, SteadfastPaladin.class, Island.class})
class GateToSeatowerTest extends BaseCardTest {

    @Test
    @DisplayName("The gate enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GateToSeatower()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gate to Seatower").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The gate produces blue mana")
    void producesBlueMana() {
        Permanent gate = addReadyGate();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The gate seeks a nonland card once")
    void seeksNonlandCardOnce() {
        Permanent gate = addReadyGate();
        SteadfastPaladin paladin = new SteadfastPaladin();
        Island island = new Island();
        harness.setLibrary(player1, List.of(island, paladin));
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(paladin);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
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
    @DisplayName("Seeking with only lands leaves the library and hand unchanged")
    void seekWithOnlyLands() {
        addReadyGate();
        Island first = new Island();
        Island second = new Island();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Each gate can seek once independently")
    void separateCopiesCanSeek() {
        addReadyGate();
        addReadyGate();
        SteadfastPaladin first = new SteadfastPaladin();
        SteadfastPaladin second = new SteadfastPaladin();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        addSeekMana();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Using seek does not disable the mana ability")
    void canProduceManaAfterSeeking() {
        Permanent gate = addReadyGate();
        harness.setLibrary(player1, List.of());
        addSeekMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        gate.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private Permanent addReadyGate() {
        return addCreatureReady(player1, new GateToSeatower());
    }

    private void addSeekMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
