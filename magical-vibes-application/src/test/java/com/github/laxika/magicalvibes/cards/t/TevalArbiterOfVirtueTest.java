package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TevalArbiterOfVirtue.class, Shock.class, ThinkTwice.class})
class TevalArbiterOfVirtueTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's spell has delve and costs life equal to its mana value")
    void grantsDelveAndLosesSpellManaValueLife() {
        Card graveyardCard = new Shock();
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new ThinkTwice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castInstantWithMultipleGraveyardExile(player1, 0, null, List.of(0));

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(graveyardCard);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Teval does not trigger for an opponent's spell")
    void doesNotTriggerForOpponentSpell() {
        harness.addToBattlefield(player1, new TevalArbiterOfVirtue());
        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ThinkTwice()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int casterLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(casterLifeBefore);
    }
}
