package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.s.ScavengerFolk;
import com.github.laxika.magicalvibes.cards.s.ScarwoodGoblins;
import com.github.laxika.magicalvibes.cards.s.SpittingSlug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Tracker.class, FountainOfYouth.class, ScavengerFolk.class, ScarwoodGoblins.class, SpittingSlug.class})
class TrackerTest extends BaseCardTest {

    @Test
    void fightsSmallerCreatureAndSurvives() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        Permanent scavengerFolk = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());

        activateTracker(tracker, scavengerFolk);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(scavengerFolk.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(tracker.getId()));
        assertThat(tracker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fightsEqualCreatureAndBothDie() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        Permanent scarwoodGoblins = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());

        activateTracker(tracker, scarwoodGoblins);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(tracker.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(scarwoodGoblins.getId()));
    }

    @Test
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new Tracker());
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.activateAbility(player1, 0, null, fountain.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWhileTapped() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        Permanent scavengerFolk = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());

        activateTracker(tracker, scavengerFolk);

        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());
        assertThatThrownBy(() -> {
            harness.addMana(player1, ManaColor.GREEN, 2);
            harness.activateAbility(player1, 0, null, otherCreature.getId());
        }).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItselfAndDealsDamageTwice() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        tracker.setToughnessModifier(3);

        activateTracker(tracker, tracker);

        assertThat(tracker.getMarkedDamage()).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(tracker.getId()));
    }

    @Test
    void targetLeavingBeforeResolutionPreventsTheFight() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(tracker.getId()));
        assertThat(tracker.getMarkedDamage()).isZero();
    }

    @Test
    void sourceLeavingBeforeResolutionUsesLastKnownPower() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        tracker.setPowerModifier(1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpittingSlug());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(tracker);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    private void activateTracker(Permanent tracker, Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
