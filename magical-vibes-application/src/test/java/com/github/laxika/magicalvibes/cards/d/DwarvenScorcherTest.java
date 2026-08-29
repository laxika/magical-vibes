package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenScorcher.class, Forest.class, GrizzlyBears.class})
class DwarvenScorcherTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndCreatureSurvives() {
        Permanent scorcher = addReadyScorcher();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateScorcher(scorcher, bears);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 18);
        assertThat(bears.getMarkedDamage()).isZero();
    }

    @Test
    void controllerDeclinesDamageAndCreatureTakesDamage() {
        Permanent scorcher = addReadyScorcher();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        activateScorcher(scorcher, bears);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player1, "Dwarven Scorcher");
        harness.assertLife(player2, 20);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent scorcher = addReadyScorcher();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> activateScorcher(scorcher, forest))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyScorcher() {
        Permanent scorcher = harness.addToBattlefieldAndReturn(player1, new DwarvenScorcher());
        scorcher.setSummoningSick(false);
        return scorcher;
    }

    private void activateScorcher(Permanent scorcher, Permanent target) {
        harness.activateAbility(player1, battlefieldIndex(player1, scorcher), 0, null, target.getId());
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
