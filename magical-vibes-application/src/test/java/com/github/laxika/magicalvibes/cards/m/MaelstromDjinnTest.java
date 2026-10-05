package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.Timecrafting;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MaelstromDjinn.class, Timecrafting.class})
class MaelstromDjinnTest extends BaseCardTest {

    @Test
    void gainsVanishingAndTimeCountersWhenTurnedFaceUp() {
        Permanent djinn = turnFaceUpDjinn();

        assertThat(djinn.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, djinn, Keyword.VANISHING)).isTrue();
    }

    @Test
    void faceDownDjinnDoesNotHaveVanishingBeforeBeingTurnedFaceUp() {
        harness.setHand(player1, List.of(new MaelstromDjinn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent djinn = findPermanent(player1, "Maelstrom Djinn");
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(djinn.isFaceDown()).isTrue();
        assertThat(djinn.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.hasKeyword(gd, djinn, Keyword.VANISHING)).isFalse();
        harness.assertOnBattlefield(player1, "Maelstrom Djinn");
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

    @Test
    void castingFaceUpDoesNotGrantVanishing() {
        harness.castFromHand(player1, new MaelstromDjinn(), "{7}{U}");
        resolveAllTriggers();
        Permanent djinn = findPermanent(player1, "Maelstrom Djinn");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(djinn.getCounterCount(CounterType.TIME)).isZero();
        assertThat(gqs.hasKeyword(gd, djinn, Keyword.VANISHING)).isFalse();
        harness.assertOnBattlefield(player1, "Maelstrom Djinn");
    }

    @Test
    void opponentsUpkeepDoesNotRemoveTimeCounters() {
        Permanent djinn = turnFaceUpDjinn();
        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(djinn.getCounterCount(CounterType.TIME)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Maelstrom Djinn");
    }

    @Test
    void removingLastTimeCounterWithAnotherSpellTriggersSacrifice() {
        Permanent djinn = turnFaceUpDjinn();
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castModalInstantForX(player1, 0, 0, 2, djinn.getId());
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Maelstrom Djinn");
        harness.assertInGraveyard(player1, "Maelstrom Djinn");
    }

    @Test
    void vanishingDoesNotTriggerAtUpkeepWithoutTimeCounters() {
        Permanent djinn = turnFaceUpDjinn();
        djinn.setCounterCount(CounterType.TIME, 0);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Maelstrom Djinn");
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
