package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RadiantKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Activated ability prevents blue and black creatures from dealing combat damage")
    void preventsBlueAndBlackCombatDamage() {
        addReadyKavu();
        Permanent blue = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        Permanent black = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, blue, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isTrue();
    }

    @Test
    @DisplayName("Activated ability does not prevent other creatures from dealing combat damage")
    void allowsOtherCreatureColorsToDealCombatDamage() {
        addReadyKavu();
        Permanent green = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent colorless = harness.addToBattlefieldAndReturn(player2, new Ornithopter());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, green, true)).isFalse();
        assertThat(gqs.isPreventedFromDealingDamage(gd, colorless, true)).isFalse();
    }

    @Test
    @DisplayName("Activated ability only prevents combat damage")
    void doesNotPreventNoncombatDamage() {
        addReadyKavu();
        Permanent black = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        activateAbility();

        assertThat(gqs.isPreventedFromDealingDamage(gd, black, false)).isFalse();
    }

    @Test
    @DisplayName("Prevention expires at the end of the turn")
    void preventionExpiresAtEndOfTurn() {
        addReadyKavu();
        Permanent black = harness.addToBattlefieldAndReturn(player2, new ScatheZombies());

        activateAbility();
        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, black, true)).isFalse();
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyKavu() {
        Permanent kavu = new Permanent(new RadiantKavu());
        kavu.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(kavu);
        return kavu;
    }
}
