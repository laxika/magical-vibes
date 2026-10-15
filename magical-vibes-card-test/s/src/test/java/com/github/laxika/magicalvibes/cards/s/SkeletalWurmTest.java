package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkeletalWurm.class, Murder.class})
class SkeletalWurmTest extends BaseCardTest {

    @Test
    void activatingRegenerationAbilityGrantsShield() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void regenerationShieldSavesWurmFromDestroyEffect() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        harness.assertOnBattlefield(player1, "Skeletal Wurm");
        assertThat(wurm.getRegenerationShield()).isZero();
    }

    @Test
    void regenerationCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new SkeletalWurm());
        wurm.setSummoningSick(true);
        wurm.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(wurm.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
        assertThat(wurm.isTapped()).isTrue();
    }

    @Test
    void creatingShieldDoesNotTapRemoveDamageOrRemoveFromCombat() {
        Permanent wurm = addWurmReady(player1);
        wurm.setMarkedDamage(1);
        wurm.setAttacking(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
        assertThat(wurm.isTapped()).isFalse();
        assertThat(wurm.getMarkedDamage()).isEqualTo(1);
        assertThat(wurm.isAttacking()).isTrue();
    }

    @Test
    void regenerationReplacesLethalDamageAndRemovesWurmFromCombat() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        wurm.setAttacking(true);
        wurm.setMarkedDamage(6);

        harness.runStateBasedActions();

        harness.assertOnBattlefield(player1, "Skeletal Wurm");
        harness.assertNotInGraveyard(player1, "Skeletal Wurm");
        assertThat(wurm.getRegenerationShield()).isZero();
        assertThat(wurm.isTapped()).isTrue();
        assertThat(wurm.getMarkedDamage()).isZero();
        assertThat(wurm.isAttacking()).isFalse();
    }

    @Test
    void oneShieldOnlyReplacesOneDestruction() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Murder(), new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player2, 0, wurm.getId());
        harness.assertOnBattlefield(player1, "Skeletal Wurm");
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        harness.assertNotOnBattlefield(player1, "Skeletal Wurm");
        harness.assertInGraveyard(player1, "Skeletal Wurm");
    }

    @Test
    void repeatedActivationsProtectAgainstSeparateDestructions() {
        Permanent wurm = addWurmReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Murder(), new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 6);

        harness.castAndResolveInstant(player2, 0, wurm.getId());
        assertThat(wurm.getRegenerationShield()).isEqualTo(1);
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        harness.assertOnBattlefield(player1, "Skeletal Wurm");
        assertThat(wurm.getRegenerationShield()).isZero();
    }
    private Permanent addWurmReady(Player player) {
        Permanent wurm = harness.addToBattlefieldAndReturn(player, new SkeletalWurm());
        wurm.setSummoningSick(false);
        return wurm;
    }
}
