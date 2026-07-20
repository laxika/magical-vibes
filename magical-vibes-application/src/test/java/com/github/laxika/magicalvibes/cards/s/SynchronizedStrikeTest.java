package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SynchronizedStrikeTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Both target creatures untap and get +2/+2")
    void twoTargetsUntapAndBoost() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        a.tap();
        b.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(a.getId(), b.getId()));
        harness.passBothPriorities();

        assertThat(a.isTapped()).isFalse();
        assertThat(b.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, a)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(4);
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void singleTargetAllowed() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        bears.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
