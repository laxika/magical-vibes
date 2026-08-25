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

@CardUsed({DwarvenDriller.class, Forest.class, GrizzlyBears.class})
class DwarvenDrillerTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndLandSurvives() {
        Permanent driller = addReadyDriller();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Forest");
        harness.assertLife(player2, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void controllerDeclinesDamageAndLandIsDestroyed() {
        Permanent driller = addReadyDriller();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 20);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent driller = addReadyDriller();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyDriller() {
        Permanent driller = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        driller.setSummoningSick(false);
        return driller;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
