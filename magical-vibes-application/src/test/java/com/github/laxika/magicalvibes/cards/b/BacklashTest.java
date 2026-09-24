package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.k.KavuAggressor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Backlash.class, KavuAggressor.class, Forest.class})
class BacklashTest extends BaseCardTest {

    @Test
    @DisplayName("Backlash taps an untapped creature and deals damage equal to its power to its controller")
    void tapsCreatureAndDealsPowerDamage() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();
        harness.castAndResolveInstant(player1, 0, kavu.getId());

        assertThat(kavu.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Backlash cannot target a tapped creature")
    void cannotTargetTappedCreature() {
        Permanent tappedKavu = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());
        tappedKavu.tap();

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, tappedKavu.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature");
    }

    @Test
    @DisplayName("Backlash fizzles if its target becomes tapped before resolution")
    void fizzlesIfTargetBecomesTappedBeforeResolution() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new KavuAggressor());

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();
        harness.castInstant(player1, 0, kavu.getId());
        kavu.tap();
        harness.passBothPriorities();

        assertThat(kavu.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Backlash cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new Backlash()));
        addBacklashMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("untapped creature");
    }

    private void addBacklashMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
