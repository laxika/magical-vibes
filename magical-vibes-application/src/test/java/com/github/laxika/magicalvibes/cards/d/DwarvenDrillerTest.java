package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantWarthog;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DwarvenDriller.class, GiantWarthog.class, KrosanVerge.class})
class DwarvenDrillerTest extends BaseCardTest {

    @Test
    void controllerAcceptsDamageAndLandSurvives() {
        Permanent driller = addReadyDriller();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        harness.assertOnBattlefield(player2, "Krosan Verge");
        harness.assertLife(player2, 18);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void controllerDeclinesDamageAndLandIsDestroyed() {
        Permanent driller = addReadyDriller();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertInGraveyard(player2, "Krosan Verge");
        harness.assertLife(player2, 20);
        assertThat(driller.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonlandPermanent() {
        Permanent driller = addReadyDriller();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GiantWarthog());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithSummoningSickness() {
        Permanent driller = harness.addToBattlefieldAndReturn(player1, new DwarvenDriller());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new KrosanVerge());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, driller), 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Creature has summoning sickness");
        assertThat(driller.isTapped()).isFalse();
    }

    @Test
    void targetsItsControllersOwnLand() {
        Permanent driller = addReadyDriller();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new KrosanVerge());

        harness.activateAbility(player1, battlefieldIndex(player1, driller), 0, null, land.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Krosan Verge");
        harness.assertLife(player1, 18);
        assertThat(driller.isTapped()).isTrue();
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
