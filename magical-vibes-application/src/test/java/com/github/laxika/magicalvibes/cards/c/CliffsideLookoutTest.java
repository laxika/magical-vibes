package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CliffsideLookout.class, GrizzlyBears.class})
class CliffsideLookoutTest extends BaseCardTest {

    @Test
    @DisplayName("Ability boosts creatures you control but not an opponent's creature")
    void abilityBoostsOwnCreatures() {
        Permanent lookout = addCreatureReady(player1, new CliffsideLookout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability boost wears off at end of turn")
    void abilityBoostWearsOff() {
        addCreatureReady(player1, new CliffsideLookout());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Lookout can activate repeatedly with cumulative boosts")
    void tappedSummoningSickLookoutCanActivateRepeatedly() {
        Permanent lookout = harness.addToBattlefieldAndReturn(player1, new CliffsideLookout());
        lookout.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(3);
        assertThat(lookout.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before resolution receive the boost; later creatures do not")
    void boostAppliesToCreaturesPresentAtResolution() {
        Permanent lookout = addCreatureReady(player1, new CliffsideLookout());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.enterBattlefieldAndReturn(player1, new CliffsideLookout());

        harness.passBothPriorities();
        Permanent afterResolution = harness.enterBattlefieldAndReturn(player1, new CliffsideLookout());

        assertThat(gqs.getEffectivePower(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lookout)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability still resolves after its source leaves the battlefield")
    void abilityResolvesWithoutItsSource() {
        Permanent lookout = addCreatureReady(player1, new CliffsideLookout());
        Permanent otherLookout = addCreatureReady(player1, new CliffsideLookout());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(lookout);
        gd.playerGraveyards.get(player1.getId()).add(lookout.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, otherLookout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherLookout)).isEqualTo(2);
    }
}
