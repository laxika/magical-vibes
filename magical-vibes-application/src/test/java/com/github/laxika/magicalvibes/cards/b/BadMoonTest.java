package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Deathlace;
import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.p.Purelace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BadMoon.class, DrudgeSkeletons.class, GrizzlyBears.class, Opalescence.class, Deathlace.class, Purelace.class})
class BadMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Own black creatures get +1/+1")
    void buffsOwnBlackCreatures() {
        harness.addToBattlefield(player1, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +1/+1")
    void buffsOpponentBlackCreatures() {
        harness.addToBattlefield(player1, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Nonblack creatures are unaffected")
    void nonblackUnaffected() {
        harness.addToBattlefield(player1, new BadMoon());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two Bad Moons give +2/+2 to black creatures")
    void twoBadMoonsStack() {
        harness.addToBattlefield(player1, new BadMoon());
        harness.addToBattlefield(player1, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Bad Moon leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent badMoon = harness.addToBattlefieldAndReturn(player1, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, badMoon));

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    @DisplayName("Bonus applies when Bad Moon resolves onto the battlefield")
    void bonusAppliesOnResolve() {
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);

        harness.castFromHand(player1, new BadMoon(), "{1}{B}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bad Moon buffs itself when it becomes a creature")
    void buffsItselfWhenItBecomesACreature() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent badMoon = harness.addToBattlefieldAndReturn(player1, new BadMoon());

        assertThat(gqs.isCreature(gd, badMoon)).isTrue();
        assertThat(gqs.getEffectivePower(gd, badMoon)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, badMoon)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature gains the bonus when it becomes black")
    void bonusAppliesWhenCreatureBecomesBlack() {
        harness.addToBattlefield(player1, new BadMoon());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        harness.setHand(player1, List.of(new Deathlace()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature loses the bonus when it stops being black")
    void bonusRemovedWhenCreatureStopsBeingBlack() {
        harness.addToBattlefield(player1, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player2, new DrudgeSkeletons());
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
        harness.setHand(player1, List.of(new Purelace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, skeletons.getId());

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing one Bad Moon leaves the other controller's bonus active")
    void removingOneSourcePreservesOtherBonus() {
        Permanent badMoon = harness.addToBattlefieldAndReturn(player1, new BadMoon());
        harness.addToBattlefield(player2, new BadMoon());
        Permanent skeletons = harness.addToBattlefieldAndReturn(player1, new DrudgeSkeletons());
        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(3);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, badMoon));

        assertThat(gqs.getEffectivePower(gd, skeletons)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, skeletons)).isEqualTo(2);
    }
}
