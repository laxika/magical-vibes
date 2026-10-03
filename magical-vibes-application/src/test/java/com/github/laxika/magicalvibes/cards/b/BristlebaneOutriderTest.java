package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AdeptWatershaper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.s.SpringleafDrum;
import com.github.laxika.magicalvibes.cards.t.TimidShieldbearer;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BristlebaneOutrider.class, TimidShieldbearer.class, AdeptWatershaper.class,
        Forest.class, MarchOfTheMachines.class, SpringleafDrum.class})
class BristlebaneOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +2/+0 after another creature enters under your control")
    void getsBoostAfterAnotherCreatureEnters() {
        harness.setHand(player1, List.of(new BristlebaneOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent outrider = findPermanent(player1, "Bristlebane Outrider");

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);

        harness.setHand(player1, List.of(new TimidShieldbearer()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new TimidShieldbearer());
        Permanent outrider = addCreatureReady(player1, new BristlebaneOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(outrider);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new AdeptWatershaper());
        Permanent outrider = addCreatureReady(player1, new BristlebaneOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(outrider);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A creature that entered before the Outrider enables the bonus")
    void getsBoostForEarlierEntry() {
        harness.enterBattlefieldAndReturn(player1, new TimidShieldbearer());
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("Multiple other creatures entering do not stack the bonus")
    void bonusDoesNotStack() {
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        harness.enterBattlefieldAndReturn(player1, new TimidShieldbearer());
        harness.enterBattlefieldAndReturn(player1, new TimidShieldbearer());

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("An opponent's creature entering does not enable the bonus")
    void opponentEntryDoesNotEnableBonus() {
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        harness.enterBattlefieldAndReturn(player2, new TimidShieldbearer());

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("The bonus ends when the turn changes even if the other creature remains")
    void bonusEndsNextTurn() {
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        harness.enterBattlefieldAndReturn(player1, new TimidShieldbearer());
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("A printed power-2 creature can block when its effective power is 3")
    void boostedSmallCreatureCanBlock() {
        Permanent blocker = addCreatureReady(player2, new TimidShieldbearer());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addCreatureReady(player1, new BristlebaneOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("A printed power-3 creature cannot block when its effective power is 2")
    void weakenedLargeCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new AdeptWatershaper());
        blocker.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        addCreatureReady(player1, new BristlebaneOutrider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A noncreature permanent entering does not enable the bonus")
    void noncreatureEntryDoesNotEnableBonus() {
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        harness.enterBattlefieldAndReturn(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("The bonus remains after the creature that enabled it dies")
    void bonusRemainsAfterOtherCreatureDies() {
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        Permanent other = harness.enterBattlefieldAndReturn(player1, new TimidShieldbearer());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(other);
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }

    @Test
    @DisplayName("An artifact entering as a creature enables the bonus")
    void animatedArtifactEntryEnablesBonus() {
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        Permanent outrider = harness.enterBattlefieldAndReturn(player1, new BristlebaneOutrider());
        Permanent drum = harness.enterBattlefieldAndReturn(player1, new SpringleafDrum());

        assertThat(gqs.isCreature(gd, drum)).isTrue();
        assertThat(gqs.getEffectivePower(gd, outrider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, outrider)).isEqualTo(5);
    }
}
