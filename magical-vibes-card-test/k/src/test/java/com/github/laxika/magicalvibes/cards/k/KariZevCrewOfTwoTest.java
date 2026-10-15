package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AmuletOfVigor;
import com.github.laxika.magicalvibes.cards.r.RagavanNimblePilferer;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KariZevCrewOfTwo.class, RagavanNimblePilferer.class, AmuletOfVigor.class})
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

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

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

    @Test
    @DisplayName("An opponent's legendary Monkey does not prevent conjuring Ragavan")
    void opposingLegendaryMonkeyDoesNotPreventConjuring() {
        addCreatureReady(player1, new KariZevCrewOfTwo());
        addCreatureReady(player2, new RagavanNimblePilferer());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Ragavan, Nimble Pilferer")).hasSize(1);
        assertThat(findPermanents(player2, "Ragavan, Nimble Pilferer")).hasSize(1);
    }

    @Test
    @CardUsed({AmuletOfVigor.class})
    @DisplayName("Conjured Ragavan enters tapped and triggers Amulet of Vigor")
    void enteringTappedTriggersAmuletOfVigor() {
        addCreatureReady(player1, new KariZevCrewOfTwo());
        harness.addToBattlefield(player1, new AmuletOfVigor());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        Permanent ragavan = findPermanent(player1, "Ragavan, Nimble Pilferer");
        assertThat(ragavan.isTapped()).isFalse();
        assertThat(ragavan.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The end-step return uses Kari Zev as its source and waits for resolution")
    void endStepReturnRetainsKariZevAsSource() {
        Permanent kariZev = addCreatureReady(player1, new KariZevCrewOfTwo());

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });
        harness.passUntil(TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(kariZev.getId());

        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Ragavan, Nimble Pilferer");
        harness.assertInHand(player1, "Ragavan, Nimble Pilferer");
    }
}
