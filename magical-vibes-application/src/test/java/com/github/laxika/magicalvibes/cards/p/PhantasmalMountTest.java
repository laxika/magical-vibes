package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PhantasmalMount.class, BalduvianBears.class, Mountain.class})
class PhantasmalMountTest extends BaseCardTest {

    private Permanent addMountReady() {
        return addCreatureReady(player1, new PhantasmalMount());
    }

    private void enterMain() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void completeCleanup() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    private int indexOf(Permanent perm) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(perm);
    }

    @Test
    @DisplayName("Gives target creature +1/+1 and flying until end of turn")
    void pumpsAndGrantsFlying() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isTrue();
        assertThat(mount.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pump and flying wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(
                () -> GameTestEngineContext.get().getBean(TurnCleanupService.class).applyCleanupResets(gd));

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Sacrifices itself when the pumped creature leaves the battlefield this turn")
    void sacrificesWhenTargetLeaves() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phantasmal Mount");
        harness.assertInGraveyard(player1, "Phantasmal Mount");
        harness.assertInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Sacrifices the pumped creature when the Mount leaves the battlefield this turn")
    void sacrificesTargetWhenMountLeaves() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mount));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phantasmal Mount");
        harness.assertNotOnBattlefield(player1, "Balduvian Bears");
        harness.assertInGraveyard(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Remains pumped if the target's toughness later becomes greater than 2")
    void remainsPumpedAfterTargetToughnessIncreases() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        bears.setToughnessModifier(bears.getToughnessModifier() + 1);

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not resolve if the target's toughness becomes greater than 2 first")
    void doesNotResolveWhenTargetToughnessIncreasesBeforeResolution() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        int basePower = gqs.getEffectivePower(gd, bears);
        int baseToughness = gqs.getEffectiveToughness(gd, bears);

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        bears.setToughnessModifier(1);
        harness.passBothPriorities();

        Permanent after = gqs.findPermanentById(gd, bears.getId());
        assertThat(gqs.getEffectivePower(gd, after)).isEqualTo(basePower);
        assertThat(gqs.getEffectiveToughness(gd, after)).isEqualTo(baseToughness + 1);
        assertThat(gqs.hasKeyword(gd, after, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Delayed sacrifice of the target expires at end of turn")
    void delayedTargetSacrificeExpiresAtEndOfTurn() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        completeCleanup();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, mount));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Phantasmal Mount");
        harness.assertOnBattlefield(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Delayed sacrifice of the source expires at end of turn")
    void delayedSourceSacrificeExpiresAtEndOfTurn() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());

        enterMain();
        harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId());
        harness.passBothPriorities();

        completeCleanup();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, bears));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phantasmal Mount");
        harness.assertInHand(player1, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot target a creature with toughness greater than 2")
    void cannotTargetToughnessAboveTwo() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player1, new BalduvianBears());
        bears.setToughnessModifier(1); // effective toughness 3

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent mount = addMountReady();
        Permanent bears = addCreatureReady(player2, new BalduvianBears());

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent mount = addMountReady();
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        enterMain();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(mount), 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
