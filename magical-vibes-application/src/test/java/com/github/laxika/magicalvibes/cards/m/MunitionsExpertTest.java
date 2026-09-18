package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    private void castMunitionsExpert() {
        harness.setHand(player1, List.of(new MunitionsExpert()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
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
