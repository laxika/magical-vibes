package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CruelRealityTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted player sacrifices their only creature at upkeep and loses no life")
    void sacrificesOnlyCreature() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Enchanted player sacrifices their only planeswalker at upkeep")
    void sacrificesOnlyPlaneswalker() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new LilianaVess());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Enchanted player with no creature or planeswalker loses 5 life")
    void losesFiveLifeWhenNothingToSacrifice() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("A noncreature, nonplaneswalker permanent does not count — player loses 5 life")
    void landDoesNotSatisfySacrifice() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player2, new Forest());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        // The land is not eligible, so nothing is sacrificed and the fallback life loss applies.
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Trigger does NOT fire during the curse controller's upkeep")
    void triggerDoesNotFireOnControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player1, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CruelReality());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }
}
