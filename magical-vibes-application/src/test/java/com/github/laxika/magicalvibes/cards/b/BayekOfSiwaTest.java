package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArnoDorian;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BayekOfSiwa.class, ArnoDorian.class, GrizzlyBears.class, Shock.class})
class BayekOfSiwaTest extends BaseCardTest {

    @Test
    @DisplayName("During your turn, other historic creatures you control have double strike")
    void grantsDoubleStrikeToOtherHistoricCreaturesDuringYourTurn() {
        Permanent bayek = addCreatureReady(player1, new BayekOfSiwa());
        Permanent historic = addCreatureReady(player1, new ArnoDorian());
        Permanent nonHistoric = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingHistoric = addCreatureReady(player2, new ArnoDorian());

        harness.forceActivePlayer(player1);

        assertThat(gqs.hasKeyword(gd, bayek, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, historic, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonHistoric, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingHistoric, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, bayek, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, historic, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Disguise casts Bayek face down with ward")
    void disguiseCastsFaceDownWithWard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        BayekOfSiwa card = new BayekOfSiwa();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bayek = findPermanent(player1, "Bayek of Siwa");
        assertThat(bayek.isFaceDown()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, bayek.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bayek of Siwa").isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("Bayek can be turned face up for its disguise cost")
    void canBeTurnedFaceUpForDisguiseCost() {
        BayekOfSiwa card = new BayekOfSiwa();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bayek = findPermanent(player1, "Bayek of Siwa");
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(bayek));

        assertThat(bayek.isFaceDown()).isFalse();
    }
}
