package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.r.RagavanNimblePilferer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KariZevCrewOfTwo.class, RagavanNimblePilferer.class})
class KariZevCrewOfTwoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking conjures Ragavan tapped and attacking")
    void attackConjuresRagavanTappedAndAttacking() {
        Permanent kariZev = addCreatureReady(player1, new KariZevCrewOfTwo());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
        });

        Permanent ragavan = findPermanents(player1, "Ragavan, Nimble Pilferer").getFirst();
        assertThat(ragavan.getCard().isToken()).isFalse();
        assertThat(ragavan.isTapped()).isTrue();
        assertThat(ragavan.isAttacking()).isTrue();
        assertThat(ragavan.getAttackTarget()).isEqualTo(kariZev.getAttackTarget());
    }

    @Test
    @DisplayName("Conjured Ragavan returns to its owner's hand at the next end step")
    void conjuredRagavanReturnsAtNextEndStep() {
        addCreatureReady(player1, new KariZevCrewOfTwo());

        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        harness.assertInHand(player1, "Ragavan, Nimble Pilferer");
    }

    @Test
    @DisplayName("Does not conjure Ragavan while a legendary Monkey is controlled")
    void doesNotConjureWithLegendaryMonkey() {
        addCreatureReady(player1, new KariZevCrewOfTwo());
        addCreatureReady(player1, new RagavanNimblePilferer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(1);
    }

    @Test
    @DisplayName("The legendary Monkey condition is checked again when the trigger resolves")
    void conditionIsCheckedAtResolution() {
        addCreatureReady(player1, new KariZevCrewOfTwo());

        declareAttackers(List.of(0));
        addCreatureReady(player1, new RagavanNimblePilferer());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(1);
    }
}
