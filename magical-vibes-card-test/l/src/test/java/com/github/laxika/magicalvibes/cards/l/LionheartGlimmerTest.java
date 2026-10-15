package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LionheartGlimmer.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class})
class LionheartGlimmerTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, creatures you control get +1/+1 until end of turn")
    void boostsCreaturesYouControlWhenYouAttack() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new LionheartGlimmer());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, glimmer.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Lionheart Glimmer");
    }

    @Test
    void multipleAttackersProduceOnlyOneBoost() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    void opponentsAttackDoesNotBoostYourCreatures() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, glimmer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, glimmer)).isEqualTo(5);
    }

    @Test
    void boostAppliesToCreaturesPresentAtResolutionOnly() {
        addCreatureReady(player1, new LionheartGlimmer());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(1));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }

    @Test
    void payingWardLetsOpponentsSpellResolve() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, glimmer.getId());
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(glimmer.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Lionheart Glimmer");
    }

    @Test
    void decliningAffordableWardCountersSpell() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, glimmer.getId());
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(glimmer.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void controllersSpellDoesNotTriggerWard() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, glimmer.getId());

        assertThat(glimmer.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void wardCountersOpponentsActivatedAbilityWithoutPayment() {
        Permanent glimmer = addCreatureReady(player1, new LionheartGlimmer());
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, glimmer.getId());
        resolveAllTriggers();

        assertThat(glimmer.getMarkedDamage()).isZero();
        assertThat(pyromancer.isTapped()).isTrue();
        harness.assertOnBattlefield(player2, "Prodigal Pyromancer");
    }
}
