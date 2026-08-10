package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Lodestone Myr")
class LodestoneMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an artifact gives +1/+1")
    void tappingArtifactBoosts() {
        Permanent myr = addCreatureReady(player1, new LodestoneMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3);
        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can tap itself to get +1/+1")
    void canTapItself() {
        Permanent myr = addCreatureReady(player1, new LodestoneMyr());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(3);
        assertThat(myr.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Each activation taps another untapped artifact")
    void eachActivationTapsAnotherUntappedArtifact() {
        addCreatureReady(player1, new LodestoneMyr());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent myr = findPermanent(player1, "Lodestone Myr");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent myr = addCreatureReady(player1, new LodestoneMyr());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, myr)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, myr)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate when all controlled artifacts are tapped")
    void cannotActivateWithTappedArtifacts() {
        Permanent myr = addCreatureReady(player1, new LodestoneMyr());
        myr.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        artifact.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without an artifact")
    void cannotActivateWithoutArtifact() {
        Permanent myr = addCreatureReady(player1, new LodestoneMyr());
        myr.tap();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
