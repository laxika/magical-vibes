package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BladeOfSharedSoulsTest extends BaseCardTest {

    @Test
    @DisplayName("For Mirrodin! creates a Rebel and its attachment trigger can copy another creature")
    void livingWeaponAndCopyTrigger() {
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.setHand(player1, List.of(new BladeOfSharedSouls()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent rebel = findPermanent(player1, "Rebel");
        Permanent blade = findPermanent(player1, "Blade of Shared Souls");
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(rebel.getId());
        assertThat(rebel.getCard().getName()).isEqualTo("Hill Giant");
        assertThat(gqs.getEffectivePower(gd, rebel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, rebel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipping Blade of Shared Souls triggers the copy choice")
    void equipTriggersCopyChoice() {
        addBladeReady();
        Permanent equipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new HillGiant());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, equipped.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(equipped.getCard().getName()).isEqualTo("Hill Giant");

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(equipped.getCard().getName()).isEqualTo("Grizzly Bears");
        harness.handlePermanentChosen(player1, equipped.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
    }

    private Permanent addBladeReady() {
        Permanent blade = new Permanent(new BladeOfSharedSouls());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blade);
        return blade;
    }
}
