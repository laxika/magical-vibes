package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(FireNationAmbushers.class)
class FireNationAmbushersTest extends BaseCardTest {

    @Test
    void canBeCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FireNationAmbushers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Nation Ambushers");
    }

    @ParameterizedTest
    @EnumSource(value = TurnStep.class, names = {"UPKEEP", "BEGINNING_OF_COMBAT", "END_STEP"})
    void canBeCastOutsideMainPhase(TurnStep step) {
        harness.forceActivePlayer(player2);
        harness.forceStep(step);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new FireNationAmbushers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fire Nation Ambushers");
    }

    @Test
    void canBeCastInResponseToAnotherCreatureSpell() {
        harness.setHand(player1, List.of(new FireNationAmbushers()));
        harness.setHand(player2, List.of(new FireNationAmbushers()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Fire Nation Ambushers");
        harness.assertOnBattlefield(player2, "Fire Nation Ambushers");
    }
}
