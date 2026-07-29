package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShaukuEndbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Shauku can attack when it is the only creature on the battlefield")
    void canAttackAlone() {
        harness.setLife(player2, 20);
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        declareAttackers(player1, List.of(findIndex(player1, shauku)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Shauku cannot attack while another creature is on the battlefield")
    void cannotAttackWithAnotherCreature() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int index = findIndex(player1, shauku);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(index)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller loses 3 life at the beginning of their upkeep")
    void upkeepLifeLoss() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new ShaukuEndbringer());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Tap ability exiles the target creature and puts a +1/+1 counter on Shauku")
    void exilesTargetAndGrowsSelf() {
        Permanent shauku = addCreatureReady(player1, new ShaukuEndbringer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, findIndex(player1, shauku), null, targetId);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Grizzly Bears")).isZero();
        assertThat(gqs.getEffectivePower(gd, shauku)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, shauku)).isEqualTo(6);
    }

    private int findIndex(com.github.laxika.magicalvibes.model.Player player, Permanent target) {
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < bf.size(); i++) {
            if (bf.get(i) == target) return i;
        }
        throw new IllegalStateException("Permanent not found on battlefield");
    }
}
