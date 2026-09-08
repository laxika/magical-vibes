package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CitadelOfPainTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> landsOf(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().hasType(com.github.laxika.magicalvibes.model.CardType.LAND))
                .toList();
    }

    @Test
    @DisplayName("Deals damage equal to the end-step player's untapped lands")
    void dealsDamageEqualToUntappedLands() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);
        landsOf(player1).getFirst().tap();

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Deals damage to the player whose end step it is")
    void damagesEndStepPlayerNotCitadelController() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals no damage when the end-step player's lands are tapped")
    void dealsNoDamageForTappedLands() {
        harness.addToBattlefield(player1, new CitadelOfPain());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Mountain());
        harness.setLife(player1, 20);
        landsOf(player1).forEach(Permanent::tap);

        advanceToEndStepTrigger(player1);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }
}
