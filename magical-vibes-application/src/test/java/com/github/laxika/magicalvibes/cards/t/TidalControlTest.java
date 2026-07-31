package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TidalControlTest extends BaseCardTest {

    @Test
    @DisplayName("Paying 2 life counters a target green spell")
    void payLifeCountersGreenSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying {2} counters a target red spell")
    void payManaCountersRedSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 20);

        HillGiant giant = new HillGiant();
        harness.setHand(player2, List.of(giant));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, 1, null, giant.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent may activate the ability, paying the life from their own total")
    void opponentMayActivate() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot counter a spell that is neither red nor green")
    void cannotTargetWhiteSpell() {
        harness.addToBattlefield(player1, new TidalControl());
        harness.setLife(player1, 20);

        SavannahLions lions = new SavannahLions();
        harness.setHand(player2, List.of(lions));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lions.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cumulative upkeep sacrifices Tidal Control when the cost is not paid")
    void cumulativeUpkeepSacrifices() {
        harness.addToBattlefield(player1, new TidalControl());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Tidal Control");
        harness.assertInGraveyard(player1, "Tidal Control");
    }
}
