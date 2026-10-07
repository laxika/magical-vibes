package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TenthDistrictGuard.class, VernadiShieldmate.class})
class TenthDistrictGuardTest extends BaseCardTest {

    @Test
    void etbGivesTargetCreaturePlusZeroPlusOneUntilEndOfTurn() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent shieldmate = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(shieldmate.getPowerModifier()).isEqualTo(0);
        assertThat(shieldmate.getToughnessModifier()).isEqualTo(1);
        assertThat(shieldmate.getEffectivePower()).isEqualTo(2);
        assertThat(shieldmate.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent shieldmate = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(shieldmate.getPowerModifier()).isEqualTo(0);
        assertThat(shieldmate.getToughnessModifier()).isEqualTo(0);
        assertThat(shieldmate.getEffectivePower()).isEqualTo(2);
        assertThat(shieldmate.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void etbCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);

        resolveAllTriggers();

        Permanent shieldmate = findPermanent(player1, "Vernadi Shieldmate");
        assertThat(shieldmate.getEffectivePower()).isEqualTo(2);
        assertThat(shieldmate.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    void etbFizzlesIfTargetCreatureLeavesBeforeResolution() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    void etbCanTargetItselfOnAnOtherwiseEmptyBattlefield() {
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent guard = findPermanent(player1, "Tenth District Guard");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, guard.getId());
        resolveAllTriggers();

        assertThat(guard.getEffectivePower()).isEqualTo(2);
        assertThat(guard.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbStillBoostsTargetAfterGuardLeavesBattlefield() {
        harness.addToBattlefield(player2, new VernadiShieldmate());
        harness.setHand(player1, List.of(new TenthDistrictGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Vernadi Shieldmate");
        harness.castCreature(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent guard = findPermanent(player1, "Tenth District Guard");
        gd.playerBattlefields.get(player1.getId()).remove(guard);
        gd.playerGraveyards.get(player1.getId()).add(guard.getCard());
        resolveAllTriggers();

        Permanent target = findPermanent(player2, "Vernadi Shieldmate");
        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

}
