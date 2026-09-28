package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.e.EsperSentinel;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TaxTaker.class, TitheTaker.class, DarkRitual.class, EsperSentinel.class,
        MindStone.class})
class TaxTakerTest extends BaseCardTest {

    @Test
    void createsTreasuresWhenAnOpponentPaysAConditionalSpellTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new TitheTaker());
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void createsTreasuresWhenAnOpponentPaysEsperSentinelTax() {
        harness.addToBattlefield(player1, new TaxTaker());
        harness.addToBattlefield(player1, new EsperSentinel());
        prepareOpponentTurn();

        harness.setHand(player2, List.of(new MindStone()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castArtifact(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    private void prepareOpponentTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
