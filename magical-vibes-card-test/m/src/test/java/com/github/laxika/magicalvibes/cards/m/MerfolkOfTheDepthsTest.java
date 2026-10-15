package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MerfolkOfTheDepths.class})
class MerfolkOfTheDepthsTest extends BaseCardTest {

    @ParameterizedTest
    @CsvSource({"2, 0", "1, 1", "0, 2"})
    void canCastDuringOpponentsCombatWithEitherHybridColor(int green, int blue) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new MerfolkOfTheDepths()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, green);
        harness.addMana(player1, ManaColor.BLUE, blue);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Merfolk of the Depths");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk of the Depths");
        harness.assertNotInHand(player1, "Merfolk of the Depths");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canRespondToAnotherCreatureSpell() {
        MerfolkOfTheDepths first = new MerfolkOfTheDepths();
        MerfolkOfTheDepths response = new MerfolkOfTheDepths();
        harness.setHand(player1, List.of(first));
        harness.setHand(player2, List.of(response));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.addMana(player2, ManaColor.BLUE, 6);

        harness.castCreature(player1, 0);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isSameAs(response);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Merfolk of the Depths");
        harness.assertNotOnBattlefield(player1, "Merfolk of the Depths");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(first);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk of the Depths");
        assertThat(gd.stack).isEmpty();
    }
}
