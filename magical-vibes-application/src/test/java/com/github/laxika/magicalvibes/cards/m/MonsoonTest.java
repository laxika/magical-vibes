package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MonsoonTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger
    }

    private List<Permanent> islandsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Island"))
                .toList();
    }

    @Test
    @DisplayName("Taps the end-step player's untapped Islands and deals that much damage to them")
    void tapsIslandsAndDealsDamage() {
        harness.addToBattlefield(player1, new Monsoon());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        advanceToEndStepTrigger(player1);

        assertThat(islandsOf(player1)).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Forest"))
                .allMatch(p -> !p.isTapped());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Already-tapped Islands are not counted toward the damage")
    void alreadyTappedIslandsDoNotCount() {
        harness.addToBattlefield(player1, new Monsoon());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Island());
        harness.setLife(player1, 20);
        islandsOf(player1).getFirst().tap();

        advanceToEndStepTrigger(player1);

        assertThat(islandsOf(player1)).allMatch(Permanent::isTapped);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("No Islands means no damage")
    void noIslandsNoDamage() {
        harness.addToBattlefield(player1, new Monsoon());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player1, 20);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Hits the end-step player, not Monsoon's controller")
    void hitsEndStepPlayerNotController() {
        harness.addToBattlefield(player2, new Monsoon());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepTrigger(player1);

        assertThat(islandsOf(player1)).allMatch(Permanent::isTapped);
        assertThat(islandsOf(player2)).allMatch(p -> !p.isTapped());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
