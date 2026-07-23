package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhantasmalMountTest extends BaseCardTest {

    private Permanent addMountReady() {
        Permanent mount = new Permanent(new PhantasmalMount());
        mount.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mount);
        return mount;
    }

    private void enterMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    @Test
    @DisplayName("Gives target creature +1/+1 and flying until end of turn")
    void pumpsAndGrantsFlying() {
        Permanent mount = addMountReady();
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, wizard.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, wizard.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isTrue();
        assertThat(mount.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mount = addMountReady();
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        int basePower = gqs.getEffectivePower(gd, wizard);
        int baseToughness = gqs.getEffectiveToughness(gd, wizard);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, wizard.getId());
        harness.passBothPriorities();

        GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd);

        Permanent after = gqs.findPermanentById(gd, wizard.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices itself when the pumped creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent mount = addMountReady();
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, wizard.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToHand(gd, wizard);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Mount");
        harness.assertInGraveyard(player1, "Phantasmal Mount");
        harness.assertInHand(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Sacrifices the pumped creature when the Mount leaves the battlefield this turn")
    void sacrificesTargetWhenMountLeaves() {
        Permanent mount = addMountReady();
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player1.getId()).add(wizard);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, wizard.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToHand(gd, mount);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phantasmal Mount");
        harness.assertNotOnBattlefield(player1, "Fugitive Wizard");
        harness.assertInGraveyard(player1, "Fugitive Wizard");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 2")
    void cannotTargetToughnessAboveTwo() {
        Permanent mount = addMountReady();
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setToughnessModifier(1); // effective toughness 3
        gd.playerBattlefields.get(player1.getId()).add(bears);

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent mount = addMountReady();
        Permanent wizard = new Permanent(new FugitiveWizard());
        gd.playerBattlefields.get(player2.getId()).add(wizard);

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, wizard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mount = addMountReady();
        Permanent mountain = new Permanent(new Mountain());
        gd.playerBattlefields.get(player1.getId()).add(mountain);

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
