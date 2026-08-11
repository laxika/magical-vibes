package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Backlash taps an untapped creature and deals damage equal to its power to its controller")
    void tapsCreatureAndDealsPowerDamage() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Backlash cannot target a tapped creature")
    void cannotTargetTappedCreature() {
        Permanent tappedBears = new Permanent(new GrizzlyBears());
        tappedBears.tap();
        harness.getGameData().playerBattlefields.get(player2.getId()).add(tappedBears);

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature");
    }

    @Test
    @DisplayName("Backlash fizzles if its target becomes tapped before resolution")
    void fizzlesIfTargetBecomesTappedBeforeResolution() {
        Permanent bears = new Permanent(new GrizzlyBears());
        harness.getGameData().playerBattlefields.get(player2.getId()).add(bears);

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();
        harness.castInstant(player1, 0, bears.getId());
        bears.tap();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    private void addBacklashMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
