package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhyrexianWarBeastTest extends BaseCardTest {

    /** Makes player2 edict away player1's only creature, the War Beast. */
    private void edictTheWarBeast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, new ArrayList<>(List.of(new CruelEdict())));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // Cruel Edict → War Beast is sacrificed
    }

    @Test
    @DisplayName("Leaving the battlefield sacrifices the sole land and deals 1 damage to its controller")
    void leavesBattlefieldSacrificesLandAndDealsDamage() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        harness.addToBattlefield(player1, new Forest());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        edictTheWarBeast();
        harness.passBothPriorities(); // damage half of the leaves-the-battlefield trigger
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // land sacrifice

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With several lands the controller chooses which one to sacrifice")
    void controllerChoosesWhichLandToSacrifice() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        edictTheWarBeast();
        harness.passBothPriorities(); // damage half of the leaves-the-battlefield trigger
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // land sacrifice → choice

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(1);

        Permanent forest = findPermanent(player1, "Forest");
        harness.handleMultiplePermanentsChosen(player1, List.of(forest.getId()));

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Mountain");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }

    @Test
    @DisplayName("With no lands the controller still takes 1 damage")
    void noLandsStillDealsDamage() {
        harness.addToBattlefield(player1, new PhyrexianWarBeast());
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        edictTheWarBeast();
        harness.passBothPriorities(); // leaves-the-battlefield trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife - 1);
    }
}
