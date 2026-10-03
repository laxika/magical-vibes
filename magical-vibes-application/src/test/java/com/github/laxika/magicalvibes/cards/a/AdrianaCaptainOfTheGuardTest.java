package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DihadaBinderOfWills;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.s.SkyhunterStrikeForce;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdrianaCaptainOfTheGuard.class, GrizzlyBears.class,
        DihadaBinderOfWills.class, SkyhunterStrikeForce.class, InvasionOfZendikar.class})
class AdrianaCaptainOfTheGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Other creatures you control have melee")
    void grantsMeleeToOtherCreaturesYouControl() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MELEE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.MELEE)).isFalse();
    }

    @Test
    @DisplayName("Melee gives an attacking creature +1/+1 until end of turn")
    void meleeBoostsAttackingCreature() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Adriana does not gain a second melee boost from her own ability")
    void doesNotAddAnotherMeleeInstanceToAdriana() {
        Permanent adriana = addCreatureReady(player1, new AdrianaCaptainOfTheGuard());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        int adrianaPowerBefore = gqs.getEffectivePower(gd, adriana);
        int adrianaToughnessBefore = gqs.getEffectiveToughness(gd, adriana);
        int otherPowerBefore = gqs.getEffectivePower(gd, otherCreature);
        int otherToughnessBefore = gqs.getEffectiveToughness(gd, otherCreature);

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, adriana)).isEqualTo(adrianaPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, adriana)).isEqualTo(adrianaToughnessBefore + 1);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(otherPowerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(otherToughnessBefore + 1);
    }

    @Test
    @DisplayName("Granted melee triggers separately from a creature's own melee")
    void multipleMeleeInstancesEachBoostTheAttacker() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent attacker = addCreatureReady(player1, new SkyhunterStrikeForce());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 2);
    }

    @Test
    @DisplayName("Attacking only a planeswalker does not count as attacking its controller for melee")
    void planeswalkerAttackDoesNotIncreaseMeleeBonus() {
        Permanent adriana = addCreatureReady(player1, new AdrianaCaptainOfTheGuard());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new DihadaBinderOfWills());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        int powerBefore = gqs.getEffectivePower(gd, adriana);
        int toughnessBefore = gqs.getEffectiveToughness(gd, adriana);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, adriana)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, adriana)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("Melee still triggers when only a battle is attacked, even with a zero bonus")
    void battleAttackStillTriggersMelee() {
        Permanent adriana = addCreatureReady(player1, new AdrianaCaptainOfTheGuard());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId())));

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(entry.getSourcePermanentId()).isEqualTo(adriana.getId());
        });
    }

    @Test
    @DisplayName("Creatures with granted melee get no bonus unless they attack")
    void nonattackingCreatureDoesNotGetMeleeBonus() {
        addCreatureReady(player1, new AdrianaCaptainOfTheGuard());
        Permanent nonattacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, nonattacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, nonattacker);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, nonattacker)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, nonattacker)).isEqualTo(toughnessBefore);
    }

    @Test
    @DisplayName("A granted melee trigger still resolves after Adriana leaves the battlefield")
    void meleeTriggerSurvivesLossOfAdriana() {
        Permanent adriana = harness.addToBattlefieldAndReturn(player1, new AdrianaCaptainOfTheGuard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, adriana);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MELEE)).isFalse();
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    @DisplayName("Melee's bonus expires at end of turn while the granted ability remains")
    void meleeBonusExpiresAtCleanup() {
        harness.addToBattlefield(player1, new AdrianaCaptainOfTheGuard());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int powerBefore = gqs.getEffectivePower(gd, attacker);
        int toughnessBefore = gqs.getEffectiveToughness(gd, attacker);

        declareAttackers(player1, List.of(1));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore + 1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore + 1);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(powerBefore);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(toughnessBefore);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.MELEE)).isTrue();
    }
}
