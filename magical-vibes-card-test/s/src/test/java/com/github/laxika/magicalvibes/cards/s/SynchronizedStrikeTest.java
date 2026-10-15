package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.h.HapatrasMark;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SynchronizedStrike.class, DuneBeetle.class, Mountain.class, HapatrasMark.class})
class SynchronizedStrikeTest extends BaseCardTest {

    private void giveMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Both target creatures untap and get +2/+2")
    void twoTargetsUntapAndBoost() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        a.tap();
        b.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(a.getId(), b.getId()));

        assertThat(a.isTapped()).isFalse();
        assertThat(b.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, a)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, a)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, b)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, b)).isEqualTo(6);
    }

    @Test
    @DisplayName("May target only one creature (up to two)")
    void singleTargetAllowed() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        beetle.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(beetle.getId()));

        assertThat(beetle.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.of(beetle.getId()));
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(3);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(4);
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

    @Test
    @DisplayName("Zero targets resolves without affecting other creatures")
    void zeroTargetsAllowed() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        beetle.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        assertThat(beetle.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Synchronized Strike");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An untapped opposing creature is a legal target and receives the boost")
    void untappedOpponentCreatureGetsBoost() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        harness.castAndResolveInstant(player1, 0, beetle.getId());

        assertThat(beetle.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot choose more than two targets")
    void threeTargetsRejected() {
        Permanent a = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent b = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        Permanent c = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(a.getId(), b.getId(), c.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose the same creature twice")
    void duplicateTargetRejected() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(beetle.getId(), beetle.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An illegal target is unaffected while the remaining target untaps and gets boosted")
    void partiallyIllegalTargets() {
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        Permanent legalCreature = harness.addToBattlefieldAndReturn(player1, new DuneBeetle());
        protectedCreature.tap();
        legalCreature.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        harness.setHand(player2, List.of(new HapatrasMark()));
        giveMana();
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), legalCreature.getId()));
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, protectedCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, protectedCreature)).isEqualTo(4);
        assertThat(legalCreature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, legalCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, legalCreature)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Synchronized Strike");
    }

    @Test
    @DisplayName("A spell whose only target gains hexproof has no effect")
    void allTargetsIllegal() {
        Permanent beetle = harness.addToBattlefieldAndReturn(player2, new DuneBeetle());
        beetle.tap();
        harness.setHand(player1, List.of(new SynchronizedStrike()));
        harness.setHand(player2, List.of(new HapatrasMark()));
        giveMana();
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, List.of(beetle.getId()));
        harness.castAndResolveInstant(player2, 0, beetle.getId());
        harness.passBothPriorities();

        assertThat(beetle.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, beetle)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, beetle)).isEqualTo(4);
        harness.assertInGraveyard(player1, "Synchronized Strike");
        assertThat(gd.stack).isEmpty();
    }
}
