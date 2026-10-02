package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LoyalPegasus;
import com.github.laxika.magicalvibes.cards.s.SwordwiseCentaur;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AspectOfHydra.class, LoyalPegasus.class, SwordwiseCentaur.class})
@DisplayName("Aspect of Hydra")
class AspectOfHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets +X/+X equal to green devotion")
    void targetCreatureGetsBoostEqualToGreenDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        harness.addToBattlefield(player1, new SwordwiseCentaur());
        harness.addToBattlefield(player1, new SwordwiseCentaur());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("Green devotion from an opponent is not counted")
    void doesNotCountOpponentsGreenDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        harness.addToBattlefield(player2, new SwordwiseCentaur());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        harness.addToBattlefield(player1, new SwordwiseCentaur());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's creature can be boosted using the caster's devotion")
    void boostsOpponentsCreatureUsingCastersDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LoyalPegasus());
        harness.addToBattlefield(player1, new SwordwiseCentaur());
        harness.addToBattlefield(player2, new SwordwiseCentaur());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Devotion is determined at resolution and the boost then stays fixed")
    void devotionIsDeterminedAtResolutionAndBoostStaysFixed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        harness.setHand(player1, List.of(new AspectOfHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        Permanent contributor = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(contributor);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("Devotion lost before resolution does not contribute to the boost")
    void devotionLostBeforeResolutionIsNotCounted() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new LoyalPegasus());
        Permanent contributor = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        harness.setHand(player1, List.of(new AspectOfHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(contributor);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target's own green mana symbols contribute to devotion")
    void targetsOwnManaSymbolsContributeToDevotion() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new SwordwiseCentaur());
        castAspect(target);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    private void castAspect(Permanent target) {
        harness.setHand(player1, List.of(new AspectOfHydra()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
