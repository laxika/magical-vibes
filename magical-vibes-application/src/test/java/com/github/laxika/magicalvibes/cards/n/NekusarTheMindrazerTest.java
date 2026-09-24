package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NekusarTheMindrazer.class, CounselOfTheSoratami.class, Forest.class})
class NekusarTheMindrazerTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws an additional card during their draw step")
    void eachPlayerDrawsAnAdditionalCard() {
        harness.addToBattlefield(player1, new NekusarTheMindrazer());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        advanceToDraw(player2);
        drainStack();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Deals 1 damage for each card an opponent draws")
    void damagesOpponentForEachCardDrawn() {
        harness.addToBattlefield(player1, new NekusarTheMindrazer());
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CounselOfTheSoratami()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castSorcery(player2, 0, 0);
        drainStack();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not deal damage when its controller draws")
    void doesNotDamageControllerForDrawing() {
        harness.addToBattlefield(player1, new NekusarTheMindrazer());
        harness.setLife(player1, 20);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        advanceToDraw(player1);
        drainStack();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void drainStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 50) {
            harness.passBothPriorities();
        }
    }
}
