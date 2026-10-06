package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BaneslayerAngel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
import com.github.laxika.magicalvibes.cards.w.WhiteDragon;
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

@CardUsed({RayOfEnfeeblement.class, BaneslayerAngel.class, GrizzlyBears.class, Forest.class,
        HillGiantHerdgorger.class, WhiteDragon.class})
class RayOfEnfeeblementTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a nonwhite creature -4/-1 until end of turn")
    void debuffsNonwhiteCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRay(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives a white creature -4/-4 instead")
    void givesWhiteCreatureStrongerDebuff() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BaneslayerAngel());

        castRay(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRay(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new RayOfEnfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A white creature with four toughness dies from the replacement debuff")
    void killsWhiteCreatureWithFourToughness() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WhiteDragon());

        castRay(target);

        harness.assertNotOnBattlefield(player2, "White Dragon");
        harness.assertInGraveyard(player2, "White Dragon");
    }

    @Test
    @DisplayName("Repeated casts stack on a nonwhite creature you control")
    void repeatedCastsStackOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());

        castRay(target);
        castRay(target);

        harness.assertOnBattlefield(player1, "Hill Giant Herdgorger");
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Repeated nonwhite debuffs can reduce toughness to zero")
    void killsNonwhiteCreatureWithRepeatedCasts() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castRay(target);
        castRay(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The white creature replacement debuff wears off at end of turn")
    void whiteDebuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BaneslayerAngel());

        castRay(target);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    private void castRay(Permanent target) {
        harness.setHand(player1, List.of(new RayOfEnfeeblement()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
