package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleetwheelCruiser.class, PrakhataPillarBug.class})
class FleetwheelCruiserTest extends BaseCardTest {

    @Test
    void entersAsAnArtifactCreatureUntilEndOfTurn() {
        harness.setHand(player1, List.of(new FleetwheelCruiser()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent cruiser = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cruiser)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, cruiser)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cruiser)).isFalse();
    }

    @Test
    void crewAnimatesCruiserAndTapsTheCrewedCreature() {
        Permanent cruiser = addCreatureReady(player1, new FleetwheelCruiser());
        Permanent crew = addCreatureReady(player1, new PrakhataPillarBug());

        harness.activateAbility(player1, 0, null, null);
        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, cruiser)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void entryAnimationUsesTheStack() {
        harness.castFromHand(player1, new FleetwheelCruiser(), "{4}");
        harness.passBothPriorities();

        Permanent cruiser = findPermanent(player1, "Fleetwheel Cruiser");
        assertThat(gqs.isCreature(gd, cruiser)).isFalse();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
    }

    @Test
    void canAttackOnTheTurnItEntersWithoutCrew() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new FleetwheelCruiser(), "{4}");
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 15);
    }

    @Test
    void trampleDealsExcessDamageToTheDefendingPlayer() {
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new FleetwheelCruiser(), "{4}");
        resolveAllTriggers();
        Permanent blocker = addCreatureReady(player2, new PrakhataPillarBug());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 3,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Prakhata Pillar-Bug");
        harness.assertOnBattlefield(player1, "Fleetwheel Cruiser");
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationExpiresAtCleanup() {
        Permanent cruiser = harness.addToBattlefieldAndReturn(player1, new FleetwheelCruiser());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new PrakhataPillarBug());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, cruiser)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, cruiser)).isFalse();
        harness.assertOnBattlefield(player1, "Fleetwheel Cruiser");
    }

    @Test
    void tappedAndOpposingCreaturesCannotPayCrewCost() {
        harness.addToBattlefield(player1, new FleetwheelCruiser());
        Permanent tappedCrew = addCreatureReady(player1, new PrakhataPillarBug());
        tappedCrew.tap();
        addCreatureReady(player2, new PrakhataPillarBug());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void animatedCruiserCannotCrewItself() {
        harness.castFromHand(player1, new FleetwheelCruiser(), "{4}");
        resolveAllTriggers();

        Permanent cruiser = findPermanent(player1, "Fleetwheel Cruiser");
        assertThat(gqs.isCreature(gd, cruiser)).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cruiser.isTapped()).isFalse();
    }
}
