package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SurtlandFlinger.class, GrizzlyBears.class, HillGiant.class})
class SurtlandFlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a Giant deals twice its power to a target player")
    void giantDealsTwiceItsPower() {
        addCreatureReady(player1, new SurtlandFlinger());
        Permanent giant = addCreatureReady(player1, new HillGiant());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, giant.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 6);
        harness.assertNotOnBattlefield(player1, "Hill Giant");
    }

    @Test
    @DisplayName("Sacrificing a non-Giant deals its power to a target player")
    void nonGiantDealsItsPower() {
        addCreatureReady(player1, new SurtlandFlinger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the sacrifice deals no damage")
    void decliningSacrificeDealsNoDamage() {
        addCreatureReady(player1, new SurtlandFlinger());
        addCreatureReady(player1, new HillGiant());
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
        harness.assertOnBattlefield(player1, "Hill Giant");
    }
}
