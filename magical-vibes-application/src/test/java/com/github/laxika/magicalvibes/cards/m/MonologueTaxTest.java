package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonologueTax.class, Shock.class})
class MonologueTaxTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure when an opponent casts their second spell each turn")
    void createsTreasureForOpponentsSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isZero();

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger when the controller casts their second spell")
    void doesNotTriggerForControllersSecondSpell() {
        harness.addToBattlefield(player1, new MonologueTax());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Treasure")).isZero();
    }
}
