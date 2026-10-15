package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PatientNaturalist;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LuxuriousLocomotive.class, PatientNaturalist.class})
class LuxuriousLocomotiveTest extends BaseCardTest {

    @Test
    void createsATreasureForEachCreatureThatCrewedIt() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void countsACrewerThatLeftTheBattlefield() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        Permanent crewer = addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, crewer));
        harness.runStateBasedActions();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void crewCanBeActivatedOnlyOnceEachTurn() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("once each turn");
    }

    @Test
    void canCrewWithMoreCreaturesThanRequired() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        Permanent first = addCreatureReady(player1, new PatientNaturalist());
        Permanent second = addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void canFinishCrewingWithoutTappingEveryAvailableCreature() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        Permanent first = addCreatureReady(player1, new PatientNaturalist());
        Permanent second = addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isFalse();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void attackTriggerStillCreatesTreasureAfterVehicleLeavesBattlefield() {
        Permanent vehicle = addCreatureReady(player1, new LuxuriousLocomotive());
        addCreatureReady(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(player1, List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vehicle));
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void summoningSickCreatureCanCrew() {
        addCreatureReady(player1, new LuxuriousLocomotive());
        Permanent crewer = harness.addToBattlefieldAndReturn(player1, new PatientNaturalist());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(crewer.isTapped()).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
