package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DesperateFarmer.class, GrizzlyBears.class, CruelEdict.class})
class DesperateFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when another creature you control dies")
    void transformsWhenAllyCreatureDies() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new DesperateFarmer());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities(); // resolve transform trigger

        assertThat(farmer.isTransformed()).isTrue();
        assertThat(farmer.getCard().getName()).isEqualTo("Depraved Harvester");
        assertThat(gqs.getEffectivePower(gd, farmer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, farmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new DesperateFarmer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent farmer = findPermanent(player1, "Desperate Farmer");
        assertThat(farmer.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Desperate Farmer itself dies")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new DesperateFarmer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Desperate Farmer");
    }

    @Test
    @DisplayName("Multiple pending death triggers do not transform the farmer back")
    void multiplePendingDeathTriggersLeaveBackFaceUp() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new DesperateFarmer());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(farmer.isTransformed()).isTrue();
        harness.passBothPriorities();
        assertThat(farmer.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Desperate Farmer gains life from combat damage")
    void frontFaceHasLifelinkInCombat() {
        Permanent farmer = addCreatureReady(player1, new DesperateFarmer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        farmer.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Depraved Harvester has lifelink and no longer triggers on allied deaths")
    void backFaceHasLifelinkAndDoesNotTransformAgain() {
        Permanent farmer = addCreatureReady(player1, new DesperateFarmer());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new DesperateFarmer());
        ally.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        assertThat(farmer.isTransformed()).isTrue();

        Permanent nextAlly = harness.addToBattlefieldAndReturn(player1, new DesperateFarmer());
        nextAlly.setMarkedDamage(2);
        harness.runStateBasedActions();
        assertThat(gd.stack).isEmpty();
        assertThat(farmer.isTransformed()).isTrue();

        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        farmer.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 16);
    }
}
