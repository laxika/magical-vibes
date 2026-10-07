package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.e.Expansion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SphinxsInsight.class, Expansion.class})
class SphinxsInsightTest extends BaseCardTest {

    @Test
    void drawsTwoCardsAndGainsLifeDuringMainPhase() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SphinxsInsight()));
        addInsightMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void drawsTwoCardsWithoutGainingLifeOutsideMainPhase() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new SphinxsInsight()));
        addInsightMana();

        int handSizeBefore = gd.playerHands.get(player1.getId()).size() - 1;
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void gainsLifeDuringPostcombatMainPhase() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new SphinxsInsight()));
        addInsightMana();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    void doesNotGainLifeDuringOpponentsMainPhase() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SphinxsInsight()));
        addInsightMana();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore);
        harness.assertLife(player2, opponentLifeBefore);
    }

    @Test
    void spellCopyDrawsCardsWithoutAddendumBonus() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        SphinxsInsight insight = new SphinxsInsight();
        harness.setHand(player1, List.of(insight, new Expansion()));
        addInsightMana();
        harness.addMana(player1, ManaColor.BLUE, 2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);
        harness.castInstant(player1, 0, 0, insight.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player1, lifeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.assertLife(player1, lifeBefore + 2);
    }

    private void addInsightMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
