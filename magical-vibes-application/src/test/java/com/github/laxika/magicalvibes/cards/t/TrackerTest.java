package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GratuitousViolence;
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

        activateTracker(scavengerFolk);

        harness.assertNotOnBattlefield(player2, "Scavenger Folk");
        harness.assertOnBattlefield(player1, "Tracker");
        assertThat(tracker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fightsEqualCreatureAndBothDie() {
        addCreatureReady(player1, new Tracker());
        Permanent scarwoodGoblins = harness.addToBattlefieldAndReturn(player2, new ScarwoodGoblins());

        activateTracker(scarwoodGoblins);

        harness.assertNotOnBattlefield(player1, "Tracker");
        harness.assertNotOnBattlefield(player2, "Scarwood Goblins");
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
        addCreatureReady(player1, new Tracker());
        Permanent scavengerFolk = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());

        activateTracker(scavengerFolk);

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

        activateTracker(tracker);

        assertThat(tracker.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Tracker");
    }

    @Test
    void targetLeavingBeforeResolutionPreventsTheFight() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ScavengerFolk());

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tracker");
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

        harness.assertOnBattlefield(player2, "Spitting Slug");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Tracker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpittingSlug());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyOneGreenMana() {
        addCreatureReady(player1, new Tracker());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpittingSlug());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetAnotherCreatureYouControl() {
        addCreatureReady(player1, new Tracker());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new ScavengerFolk());

        activateTracker(target);

        harness.assertInGraveyard(player1, "Scavenger Folk");
        harness.assertOnBattlefield(player1, "Tracker");
        assertThat(findPermanent(player1, "Tracker").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void usesBothCreaturesPowerAtResolution() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        tracker.setToughnessModifier(3);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpittingSlug());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        tracker.setPowerModifier(1);
        target.setPowerModifier(1);

        harness.passBothPriorities();

        assertThat(tracker.isTapped()).isTrue();
        assertThat(tracker.getMarkedDamage()).isEqualTo(3);
        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Tracker");
        harness.assertOnBattlefield(player2, "Spitting Slug");
    }

    @Test
    @CardUsed(GratuitousViolence.class)
    void damageDoublingForTrackerDoesNotDoubleOpponentsReturnDamage() {
        Permanent tracker = addCreatureReady(player1, new Tracker());
        tracker.setToughnessModifier(3);
        harness.addToBattlefield(player1, new GratuitousViolence());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SpittingSlug());

        activateTracker(target);

        harness.assertInGraveyard(player2, "Spitting Slug");
        harness.assertOnBattlefield(player1, "Tracker");
        assertThat(tracker.getMarkedDamage()).isEqualTo(2);
    }

    private void activateTracker(Permanent target) {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }
}
