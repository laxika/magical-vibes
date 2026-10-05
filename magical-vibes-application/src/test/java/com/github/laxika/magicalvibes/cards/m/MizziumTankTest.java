package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MizziumTank.class, GrizzlyBears.class, Shock.class})
class MizziumTankTest extends BaseCardTest {

    @Test
    void castingNoncreatureSpellAnimatesAndBoostsTank() {
        Permanent tank = addTankReady();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(3);

        endTurn();

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void castingCreatureSpellDoesNotAnimateOrBoostTank() {
        Permanent tank = addTankReady();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void crewAnimatesTankAndTapsTheCrewCreature() {
        Permanent tank = addTankReady();
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void triggerResolvesBeforeTheSpellThatCausedIt() {
        Permanent tank = addTankReady();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(3);
        harness.assertLife(player2, 20);

        resolveAllTriggers();
        harness.assertLife(player2, 18);
    }

    @Test
    void eachNoncreatureSpellAddsAnotherBoost() {
        Permanent tank = addTankReady();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(4);

        endTurn();
        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAnimateTank() {
        Permanent tank = addTankReady();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void noncreatureSpellBoostsAnAlreadyCrewedTank() {
        Permanent tank = addTankReady();
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, tank)).isTrue();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(3);
    }

    @Test
    void crewAfterSpellTriggerPreservesTheBoostAndExpiresAtEndOfTurn() {
        Permanent tank = addTankReady();
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(3);
        endTurn();
        assertThat(gqs.isCreature(gd, tank)).isFalse();
        assertThat(gqs.getEffectivePower(gd, tank)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tank)).isEqualTo(2);
    }

    @Test
    void summoningSickCreatureCanCrewTank() {
        Permanent tank = addTankReady();
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        crew.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(crew.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, tank)).isTrue();
        endTurn();
        assertThat(gqs.isCreature(gd, tank)).isFalse();
    }

    @Test
    void animatedTankTramplesOverABlocker() {
        addTankReady();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 2));

        harness.assertLife(player2, 16);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Mizzium Tank");
    }

    private Permanent addTankReady() {
        Permanent tank = addCreatureReady(player1, new MizziumTank());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return tank;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
