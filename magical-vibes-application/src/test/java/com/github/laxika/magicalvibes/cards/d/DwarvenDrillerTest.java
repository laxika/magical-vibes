package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RiftstonePortal;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenDriller.class, DwarvenScorcher.class, RiftstonePortal.class})
class DwarvenDrillerTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndLandSurvives() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Riftstone Portal");
        harness.assertLife(player2, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnLandAndDamageItsController() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Riftstone Portal");
        harness.assertLife(player1, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void controllerDeclinesDamageAndLandIsDestroyed() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new RiftstonePortal());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Riftstone Portal");
        harness.assertLife(player2, 20);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent driller = addCreatureReady(player1, new DwarvenDriller());
        Permanent scorcher = harness.addToBattlefieldAndReturn(player2, new DwarvenScorcher());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, scorcher.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
