package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.p.PoisonDartFrog;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalametWarScribe.class, PoisonDartFrog.class})
class MalametWarScribeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering gives creatures you control +2/+1 until end of turn")
    void boostsOwnCreatures() {
        harness.addToBattlefield(player1, new PoisonDartFrog());

        cast(player1);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Poison Dart Frog"))).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Poison Dart Frog"))).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player2, new PoisonDartFrog());

        cast(player1);

        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Poison Dart Frog"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player2, "Poison Dart Frog"))).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new PoisonDartFrog());

        cast(player1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Poison Dart Frog"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Poison Dart Frog"))).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures entering before the trigger resolves receive the boost")
    void boostsCreaturesEnteringBeforeResolution() {
        harness.castFromHand(player1, new MalametWarScribe(), "{3}{W}{W}");
        harness.passBothPriorities();

        var frog = harness.enterBattlefieldAndReturn(player1, new PoisonDartFrog());
        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(1);

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(2);
    }

    @Test
    @DisplayName("Creatures entering after the trigger resolves do not receive the boost")
    void doesNotBoostCreaturesEnteringAfterResolution() {
        cast(player1);

        var frog = harness.enterBattlefieldAndReturn(player1, new PoisonDartFrog());

        assertThat(gqs.getEffectivePower(gd, frog)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, frog)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "Malamet War Scribe"))).isEqualTo(4);
    }

    private void cast(Player player) {
        harness.castFromHand(player, new MalametWarScribe(), "{3}{W}{W}");
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
