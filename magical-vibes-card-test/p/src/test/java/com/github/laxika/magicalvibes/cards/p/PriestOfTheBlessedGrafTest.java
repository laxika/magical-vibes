package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PriestOfTheBlessedGraf.class, Forest.class})
class PriestOfTheBlessedGrafTest extends BaseCardTest {

    @Test
    void createsOneSpiritForAnOpponentWithMoreLands() {
        harness.addToBattlefield(player1, new PriestOfTheBlessedGraf());
        harness.addToBattlefield(player2, new Forest());

        resolveControllerEndStep();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void createsNoSpiritsWhenNoOpponentHasMoreLands() {
        harness.addToBattlefield(player1, new PriestOfTheBlessedGraf());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        resolveControllerEndStep();

        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    private void resolveControllerEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
