package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GhostlyPrison;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WhenWeWereYoung.class, GrizzlyBears.class, Ornithopter.class, GhostlyPrison.class})
class WhenWeWereYoungTest extends BaseCardTest {

    @Test
    @DisplayName("Up to two target creatures each get +2/+2")
    void boostsBothTargets() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());

        castWhenWeWereYoung(List.of(first.getId(), second.getId()));

        assertThat(first.getEffectivePower()).isEqualTo(4);
        assertThat(first.getEffectiveToughness()).isEqualTo(4);
        assertThat(second.getEffectivePower()).isEqualTo(4);
        assertThat(second.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Grants lifelink when the controller has an artifact and an enchantment")
    void grantsLifelinkWithArtifactAndEnchantment() {
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefieldAndReturn(player1, new GhostlyPrison());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Does not grant lifelink without both an artifact and an enchantment")
    void doesNotGrantLifelinkWithoutBothPermanentTypes() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castWhenWeWereYoung(List.of(target.getId()));

        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("The boost and lifelink expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        harness.addToBattlefieldAndReturn(player1, new GhostlyPrison());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castWhenWeWereYoung(List.of(target.getId()));
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GhostlyPrison());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castWhenWeWereYoung(List<java.util.UUID> targets) {
        prepareCast();
        harness.castInstant(player1, 0, targets);
        harness.passBothPriorities();
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WhenWeWereYoung()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
