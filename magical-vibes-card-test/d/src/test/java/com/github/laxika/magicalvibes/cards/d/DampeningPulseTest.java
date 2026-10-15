package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CoralhelmGuide;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DampeningPulse.class, CoralhelmGuide.class})
class DampeningPulseTest extends BaseCardTest {

    @Test
    @DisplayName("Opponent creatures get -1/-0")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new DampeningPulse());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Own creatures are unaffected")
    void doesNotAffectOwnCreatures() {
        harness.addToBattlefield(player1, new DampeningPulse());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Penalty is removed when Dampening Pulse leaves")
    void penaltyIsRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new DampeningPulse());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Dampening Pulse"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Penalty begins when Dampening Pulse resolves, not while it is on the stack")
    void penaltyBeginsOnResolution() {
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        harness.castFromHand(player1, new DampeningPulse(), "{3}{U}");
        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(2);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dampening Pulse");
        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creatures entering after Dampening Pulse are also affected")
    void affectsCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new DampeningPulse());

        Permanent guide = harness.enterBattlefieldAndReturn(player2, new CoralhelmGuide());

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple copies stack and can reduce power below zero without killing a creature")
    void multipleCopiesStackBelowZero() {
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player1, new DampeningPulse());
        Permanent guide = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        assertThat(gqs.getEffectivePower(gd, guide)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, guide)).isEqualTo(1);
        harness.runStateBasedActions();
        harness.assertOnBattlefield(player2, "Coralhelm Guide");
    }

    @Test
    @DisplayName("Each player's Dampening Pulse affects only the other player's creatures")
    void opposingCopiesUseTheirOwnControllers() {
        harness.addToBattlefield(player1, new DampeningPulse());
        harness.addToBattlefield(player2, new DampeningPulse());
        Permanent ownGuide = harness.addToBattlefieldAndReturn(player1, new CoralhelmGuide());
        Permanent opposingGuide = harness.addToBattlefieldAndReturn(player2, new CoralhelmGuide());

        assertThat(gqs.getEffectivePower(gd, ownGuide)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingGuide)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ownGuide)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuide)).isEqualTo(1);
    }
}
