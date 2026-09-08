package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChorusOfTheConclave.class})
class ChorusOfTheConclaveTest extends BaseCardTest {

    @Test
    void givesAnyPlayersCreatureSpellAdditionalCountersForManaPaid() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();
        castCreatureWithChorusPayment(player2, creatureSpell, 2);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureSpell.getId())
                        && permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 2);
    }

    @Test
    void payingZeroIsAllowed() {
        harness.addToBattlefield(player1, new ChorusOfTheConclave());
        ChorusOfTheConclave creatureSpell = new ChorusOfTheConclave();
        harness.setHand(player2, List.of(creatureSpell));
        addManaForChorus(player2, 0);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        gs.playCard(gd, player2, 0, 0, null, null,
                List.of(), List.of(), false, null, null, List.of(), null, null,
                false, null, null, List.of(), List.of(), List.of(), false);
        harness.passBothPriorities();

        Permanent entered = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creatureSpell.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castCreatureWithChorusPayment(Player player, Card card, int additionalGenericMana) {
        harness.setHand(player, List.of(card));
        addManaForChorus(player, additionalGenericMana);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        List<String> repeatedAdditionalCosts = java.util.Collections.nCopies(additionalGenericMana, "{1}");
        gs.playCard(gd, player, 0, 0, null, null,
                List.of(), List.of(), false, null, null, List.of(), null, null,
                false, null, null, List.of(), List.of(),
                repeatedAdditionalCosts, false);
    }

    private void addManaForChorus(Player player, int additionalGenericMana) {
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 4 + additionalGenericMana);
    }
}
