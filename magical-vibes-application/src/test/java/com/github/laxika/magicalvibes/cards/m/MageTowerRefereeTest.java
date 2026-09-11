package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GloryscaleViashino;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MageTowerRefereeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell puts a +1/+1 counter on Mage Tower Referee")
    void multicoloredSpellAddsCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.setHand(player1, List.of(new GloryscaleViashino()));
        addGloryscaleMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve the cast trigger

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a monocolored spell does not put a counter on Mage Tower Referee")
    void monocoloredSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a multicolored spell does not trigger Mage Tower Referee")
    void opponentMulticoloredSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new MageTowerReferee());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GloryscaleViashino()));
        addGloryscaleMana(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        Permanent referee = findPermanent(player1, "Mage Tower Referee");
        assertThat(referee.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addGloryscaleMana(Player player) {
        harness.addMana(player, ManaColor.RED, 2);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.WHITE, 1);
    }
}
