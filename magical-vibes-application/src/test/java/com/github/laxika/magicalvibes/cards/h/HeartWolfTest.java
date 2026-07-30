package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DwarvenSoldier;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeartWolfTest extends BaseCardTest {

    private Permanent addWolfReady() {
        Permanent wolf = new Permanent(new HeartWolf());
        wolf.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wolf);
        return wolf;
    }

    private Permanent addDwarf() {
        Permanent dwarf = new Permanent(new DwarvenSoldier());
        gd.playerBattlefields.get(player1.getId()).add(dwarf);
        return dwarf;
    }

    private void enterCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Pumps target Dwarf and grants first strike during combat")
    void pumpsDwarfDuringCombat() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        int basePower = gqs.getEffectivePower(gd, dwarf);
        int baseToughness = gqs.getEffectiveToughness(gd, dwarf);

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, dwarf.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(wolf.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate outside combat")
    void cannotActivateOutsideCombat() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-Dwarf creature")
    void cannotTargetNonDwarf() {
        Permanent wolf = addWolfReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(bears);

        enterCombat();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(wolf), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacrifices itself when the boosted Dwarf leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, dwarf));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Heart Wolf");
        harness.assertInGraveyard(player1, "Heart Wolf");
    }

    @Test
    @DisplayName("Stays on the battlefield while the boosted Dwarf remains")
    void doesNotSacrificeIfTargetStays() {
        Permanent wolf = addWolfReady();
        Permanent dwarf = addDwarf();

        enterCombat();
        harness.activateAbility(player1, indexOf(wolf), 0, null, dwarf.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Heart Wolf");
        harness.assertOnBattlefield(player1, "Dwarven Soldier");
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }
}
