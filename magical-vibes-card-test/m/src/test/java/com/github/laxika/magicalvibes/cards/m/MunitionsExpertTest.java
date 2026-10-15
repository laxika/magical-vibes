package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MunitionsExpert.class, GoblinPiker.class, GrizzlyBears.class, LilianaVess.class})
class MunitionsExpertTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of Goblins controlled")
    void dealsDamageEqualToGoblinCount() {
        harness.addToBattlefield(player1, new GoblinPiker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMunitionsExpert();
        selectTarget(bears);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can damage a planeswalker")
    void damagesPlaneswalker() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 4);

        castMunitionsExpert();
        selectTarget(liliana);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining the may ability deals no damage")
    void decliningDealsNoDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMunitionsExpert();
        selectTarget(bears);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Only creatures and planeswalkers are legal targets")
    void onlyCreatureAndPlaneswalkerTargetsAreLegal() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 4);

        castMunitionsExpert();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(bears.getId(), liliana.getId())
                .doesNotContain(player1.getId(), player2.getId());
    }

    @Test
    @DisplayName("Opposing Goblins and controlled non-Goblins do not increase damage")
    void countsOnlyControlledGoblins() {
        harness.addToBattlefield(player2, new MunitionsExpert());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castMunitionsExpert();
        selectTarget(bears);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Goblins entering after the trigger is stacked count at resolution")
    void countsGoblinsAddedBeforeResolution() {
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        castMunitionsExpert();
        harness.handlePermanentChosen(player1, liliana.getId());
        harness.addToBattlefield(player1, new MunitionsExpert());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Goblins leaving before resolution no longer count")
    void excludesGoblinsThatLeftBeforeResolution() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new MunitionsExpert());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMunitionsExpert();
        harness.handlePermanentChosen(player1, bears.getId());
        goblin.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The trigger resolves for zero damage when its source was the last Goblin and dies")
    void dealsZeroDamageAfterLastGoblinLeaves() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castMunitionsExpert();
        harness.handlePermanentChosen(player1, bears.getId());
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();
        source.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Munitions Expert");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Munitions Expert can target itself")
    void canTargetItself() {
        castMunitionsExpert();
        Permanent source = gd.playerBattlefields.get(player1.getId()).getFirst();

        selectTarget(source);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Munitions Expert");
        harness.assertNotOnBattlefield(player1, "Munitions Expert");
    }

    private void castMunitionsExpert() {
        harness.castFromHand(player1, new MunitionsExpert(), "{B}{R}");
        harness.passBothPriorities();
    }

    private void selectTarget(Permanent target) {
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }
}
