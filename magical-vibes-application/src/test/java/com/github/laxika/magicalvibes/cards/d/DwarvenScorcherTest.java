package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenScorcher.class, DwarvenDriller.class, KrosanVerge.class})
class DwarvenScorcherTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndCreatureSurvives() {
        Permanent scorcher = addReadyScorcher();
        Permanent driller = harness.addToBattlefieldAndReturn(player2, new DwarvenDriller());

        activateScorcher(scorcher, driller);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Dwarven Driller");
        harness.assertLife(player2, 18);
        assertThat(driller.getMarkedDamage()).isZero();
    }

    @Test
    void controllerDeclinesDamageAndCreatureTakesDamage() {
        Permanent scorcher = addReadyScorcher();
        Permanent driller = harness.addToBattlefieldAndReturn(player2, new DwarvenDriller());

        activateScorcher(scorcher, driller);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.assertLife(player2, 20);
        assertThat(driller.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent scorcher = addReadyScorcher();
        Permanent verge = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThatThrownBy(() -> activateScorcher(scorcher, verge))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void invalidTargetDoesNotPaySacrificeCost() {
        Permanent scorcher = addReadyScorcher();
        Permanent verge = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThatThrownBy(() -> activateScorcher(scorcher, verge))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Dwarven Scorcher");
        harness.assertNotInGraveyard(player1, "Dwarven Scorcher");
    }

    @Test
    void targetControllerMakesTheChoiceForTheirOwnCreature() {
        Permanent scorcher = addReadyScorcher();
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());

        activateScorcher(scorcher, driller);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Dwarven Driller");
    }

    @Test
    void canActivateWithSummoningSicknessAndSacrificesAsCost() {
        Permanent scorcher = harness.addToBattlefieldAndReturn(player1, new DwarvenScorcher());
        Permanent driller = harness.addToBattlefieldAndReturn(player2, new DwarvenDriller());

        activateScorcher(scorcher, driller);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 18);
        assertThat(driller.getMarkedDamage()).isZero();
    }

    @Test
    void decliningDamageIsLethalToOneToughnessCreature() {
        Permanent scorcher = addReadyScorcher();
        Permanent target = addCreatureReady(player2, new DwarvenScorcher());

        activateScorcher(scorcher, target);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.assertNotOnBattlefield(player2, "Dwarven Scorcher");
        harness.assertInGraveyard(player2, "Dwarven Scorcher");
        harness.assertLife(player2, 20);
    }

    @Test
    void mayTargetItselfButFizzesAfterSacrificeCost() {
        Permanent scorcher = addReadyScorcher();

        activateScorcher(scorcher, scorcher);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent addReadyScorcher() {
        return addCreatureReady(player1, new DwarvenScorcher());
    }

    private void activateScorcher(Permanent scorcher, Permanent target) {
        harness.activateAbility(player1, battlefieldIndex(player1, scorcher), 0, null, target.getId());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
