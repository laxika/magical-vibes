package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DazzlingBeauty;
import com.github.laxika.magicalvibes.cards.f.FlashFoliage;
import com.github.laxika.magicalvibes.cards.g.Guma;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViashinoWeaponsmith.class, Guma.class})
class ViashinoWeaponsmithTest extends BaseCardTest {

    @Test
    @DisplayName("When Viashino Weaponsmith becomes blocked by a creature, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        weaponsmith.setAttacking(true);
        addCreatureReady(player2, new Guma());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(weaponsmith.getPowerModifier()).isEqualTo(2);
        assertThat(weaponsmith.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Viashino Weaponsmith is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        weaponsmith.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(weaponsmith.getPowerModifier()).isZero();
        assertThat(weaponsmith.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("When Viashino Weaponsmith becomes blocked by two creatures, it gets +2/+2 for each blocker")
    void becomesBlockedByMultipleCreaturesGetsBoostForEachBlocker() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        weaponsmith.setAttacking(true);
        addCreatureReady(player2, new Guma());
        addCreatureReady(player2, new Guma());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(weaponsmith.getPowerModifier()).isEqualTo(4);
        assertThat(weaponsmith.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @CardUsed(DazzlingBeauty.class)
    @DisplayName("Becoming blocked without a creature does not trigger Viashino Weaponsmith")
    void blockedWithoutCreatureDoesNotTrigger() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        addCreatureReady(player2, new Guma());
        declareAttackers(List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.setHand(player2, List.of(new DazzlingBeauty()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, weaponsmith.getId());
        resolveAllTriggers();

        assertThat(weaponsmith.isBlockedWithoutBlockers()).isTrue();
        assertThat(weaponsmith.getPowerModifier()).isZero();
        assertThat(weaponsmith.getToughnessModifier()).isZero();
    }

    @Test
    @CardUsed(FlashFoliage.class)
    @DisplayName("A creature token entering the battlefield blocking Viashino Weaponsmith triggers it")
    void creatureEnteringAsBlockerTriggers() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        addCreatureReady(player2, new Guma());
        declareAttackers(List.of(0));

        harness.setHand(player2, List.of(new FlashFoliage()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0, weaponsmith.getId());
        resolveAllTriggers();

        assertThat(weaponsmith.getPowerModifier()).isEqualTo(2);
        assertThat(weaponsmith.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent weaponsmith = addCreatureReady(player1, new ViashinoWeaponsmith());
        weaponsmith.setAttacking(true);
        addCreatureReady(player2, new Guma());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weaponsmith.getPowerModifier()).isZero();
        assertThat(weaponsmith.getToughnessModifier()).isZero();
    }

}
