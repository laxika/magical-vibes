package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MaelstromDjinn.class)
class MaelstromDjinnTest extends BaseCardTest {

    @Test
    void gainsVanishingAndTimeCountersWhenTurnedFaceUp() {
        Permanent djinn = turnFaceUpDjinn();

        assertThat(djinn.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, djinn, Keyword.VANISHING)).isTrue();
    }

    @Test
    void vanishingRemovesCountersAndSacrificesOnLastCounter() {
        Permanent djinn = turnFaceUpDjinn();

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(djinn.getCounterCount(CounterType.TIME)).isEqualTo(1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Maelstrom Djinn");
        harness.assertInGraveyard(player1, "Maelstrom Djinn");
    }

    private Permanent turnFaceUpDjinn() {
        harness.setHand(player1, List.of(new MaelstromDjinn()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent djinn = findPermanent(player1, "Maelstrom Djinn");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(djinn));
        harness.passBothPriorities();
        return djinn;
    }
}
