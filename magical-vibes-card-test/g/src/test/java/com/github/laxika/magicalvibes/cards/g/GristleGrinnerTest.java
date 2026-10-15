package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GristleGrinner.class, Deathmark.class, BorealDruid.class})
class GristleGrinnerTest extends BaseCardTest {

    @Test
    @DisplayName("Each simultaneous creature death gives a separate boost")
    void getsSeparateBoostsForSimultaneousDeaths() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent ownVictim = harness.addToBattlefieldAndReturn(player1, new BorealDruid());
        Permanent opposingVictim = harness.addToBattlefieldAndReturn(player2, new BorealDruid());
        ownVictim.setMarkedDamage(1);
        opposingVictim.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(7);
    }

    @Test
    @DisplayName("Its own death still puts its ability on the stack")
    void triggersForItsOwnDeath() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        grinner.setMarkedDamage(3);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Gristle Grinner");
        harness.assertNotOnBattlefield(player1, "Gristle Grinner");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Gristle Grinner");
    }

    @Test
    @DisplayName("Gets +2/+2 whenever a creature dies")
    void getsBoostWhenCreatureDies() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(5);
    }

    @Test
    @DisplayName("The death boost lasts until end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, victim.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(3);
    }

    @Test
    @DisplayName("Triggers when a creature its controller controls dies")
    void getsBoostWhenOwnCreatureDies() {
        Permanent grinner = harness.addToBattlefieldAndReturn(player1, new GristleGrinner());
        Permanent victim = harness.addToBattlefieldAndReturn(player1, new BorealDruid());

        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveSorcery(player1, 0, victim.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, grinner)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, grinner)).isEqualTo(5);
    }
}
