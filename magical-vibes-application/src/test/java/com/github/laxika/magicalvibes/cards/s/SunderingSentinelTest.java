package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SunderingSentinel.class)
class SunderingSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB uses intensity, then intensifies, and returns at the next upkeep with haste")
    void etbAndDelayedReturnUsePersistentIntensity() {
        SunderingSentinel sentinel = new SunderingSentinel();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(sentinel));
        addSentinelMana();

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getCardIntensity(sentinel.getId())).isEqualTo(3);

        advanceToEndStep();
        assertThat(findSentinel(player1)).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(sentinel);

        advanceToControllerUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent returned = findSentinel(player1);
        assertThat(returned).isNotNull();
        assertThat(returned.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.getCardIntensity(sentinel.getId())).isEqualTo(4);
    }

    private void addSentinelMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToControllerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findSentinel(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Sundering Sentinel"))
                .findFirst()
                .orElse(null);
    }
}
