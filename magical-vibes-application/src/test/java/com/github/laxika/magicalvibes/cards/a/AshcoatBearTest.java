package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AshcoatBear.class})
class AshcoatBearTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast during an opponent's turn because it has flash")
    void canCastDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(AshcoatBear.class);
    }

    @Test
    @DisplayName("Can cast during combat because it has flash")
    void canCastDuringCombat() {
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(AshcoatBear.class);
    }

    @Test
    @DisplayName("Resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof AshcoatBear);
    }

    @Test
    @DisplayName("Can respond to an opponent's spell and resolves before it")
    void canRespondToOpponentsSpell() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        AshcoatBear opponentsBear = new AshcoatBear();
        AshcoatBear respondingBear = new AshcoatBear();
        harness.castFromHand(player2, opponentsBear, "{1}{G}");
        harness.castFromHand(player1, respondingBear, "{1}{G}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard()).isSameAs(respondingBear);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isSameAs(opponentsBear);
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }
}
