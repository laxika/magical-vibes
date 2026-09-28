package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({GateToTumbledown.class, GrizzlyBears.class, Mountain.class})
class GateToTumbledownTest extends BaseCardTest {

    @Test
    @DisplayName("The gate enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new GateToTumbledown()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Gate to Tumbledown").isTapped()).isTrue();
    }

    @Test
    @DisplayName("The gate produces red mana")
    void producesRedMana() {
        Permanent gate = addReadyGate();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The gate seeks a nonland card once")
    void seeksNonlandCardOnce() {
        Permanent gate = addReadyGate();
        GrizzlyBears bears = new GrizzlyBears();
        Mountain mountain = new Mountain();
        harness.setLibrary(player1, List.of(mountain, bears));
        addSeekMana();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(mountain);
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

    private Permanent addReadyGate() {
        Permanent gate = new Permanent(new GateToTumbledown());
        gate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(gate);
        return gate;
    }

    private void addSeekMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
