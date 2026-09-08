package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AxelrodGunnarsonTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life and deals 1 damage to the chosen player when a damaged creature dies")
    void damagedCreatureDiesTriggersLifeGainAndPlayerDamage() {
        harness.addToBattlefield(player1, new AxelrodGunnarson());
        GrizzlyBears blockerCard = new GrizzlyBears();
        blockerCard.setPower(0);
        blockerCard.setToughness(5);
        harness.addToBattlefield(player2, blockerCard);

        Permanent axelrod = findPermanent(player1, "Axelrod Gunnarson");
        Permanent blocker = findPermanent(player2, "Grizzly Bears");
        axelrod.setSummoningSick(false);
        axelrod.setAttacking(true);
        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(axelrod))));
        resolveCombat(player1);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(blocker.getId(), 5));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId())
                .doesNotContain(blocker.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
