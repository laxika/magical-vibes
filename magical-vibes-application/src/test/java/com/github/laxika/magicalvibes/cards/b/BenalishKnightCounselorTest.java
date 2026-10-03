package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UnderbridgeWarlock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BenalishKnightCounselor.class, GrizzlyBears.class})
class BenalishKnightCounselorTest extends BaseCardTest {

    @Test
    @DisplayName("Enlisting grants a one-time boon to the next creature spell")
    void enlistingEmpowersNextCreatureSpell() {
        addCreatureReady(player1, new BenalishKnightCounselor());
        Permanent supporter = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent first = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent != supporter)
                .findFirst()
                .orElseThrow();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.nextCreatureSpellEmpowerments.getOrDefault(player1.getId(), List.of())).isEmpty();
    }

    @Test
    void boonCreatesRespondableCreatureCastTrigger() {
        addCreatureReady(player1, new BenalishKnightCounselor());
        Permanent supporter = addCreatureReady(player1, new BenalishKnightCounselor());
        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new BenalishKnightCounselor(), "{W}");

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void decliningEnlistDoesNotGrantBoon() {
        addCreatureReady(player1, new BenalishKnightCounselor());
        addCreatureReady(player1, new BenalishKnightCounselor());
        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of());
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        BenalishKnightCounselor creature = new BenalishKnightCounselor();
        harness.castFromHand(player1, creature, "{W}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(creature.getId()))
                .singleElement().satisfies(p ->
                        assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void multipleBoonsApplyToSameNextCreatureOnly() {
        addCreatureReady(player1, new BenalishKnightCounselor());
        addCreatureReady(player1, new BenalishKnightCounselor());
        Permanent firstSupporter = addCreatureReady(player1, new BenalishKnightCounselor());
        Permanent secondSupporter = addCreatureReady(player1, new BenalishKnightCounselor());
        declareAttackers(List.of(0, 1));
        harness.handleMultiplePermanentsChosen(player1, List.of(firstSupporter.getId()));
        harness.handleMultiplePermanentsChosen(player1, List.of(secondSupporter.getId()));
        resolveAllTriggers();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        BenalishKnightCounselor first = new BenalishKnightCounselor();
        harness.castFromHand(player1, first, "{W}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(first.getId()))
                .singleElement().satisfies(p ->
                        assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));

        BenalishKnightCounselor second = new BenalishKnightCounselor();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, second, "{W}");
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(second.getId()))
                .singleElement().satisfies(p ->
                        assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @CardUsed({BenalishKnightCounselor.class, UnderbridgeWarlock.class})
    void unconsumedBoonIsRecognizedByBoonCondition() {
        addCreatureReady(player1, new BenalishKnightCounselor());
        Permanent supporter = addCreatureReady(player1, new BenalishKnightCounselor());
        declareAttackers(List.of(0));
        harness.handleMultiplePermanentsChosen(player1, List.of(supporter.getId()));
        resolveAllTriggers();

        harness.addToBattlefield(player1, new UnderbridgeWarlock());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new BenalishKnightCounselor(),
                new BenalishKnightCounselor(), new BenalishKnightCounselor(),
                new BenalishKnightCounselor()));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }
}
