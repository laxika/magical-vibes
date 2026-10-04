package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.c.CultivatorsCaravan;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RustShieldRampager;
import com.github.laxika.magicalvibes.cards.t.ThreeTreeMascot;
import com.github.laxika.magicalvibes.cards.t.TreetopSentries;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HuntersTalent.class, GrizzlyBears.class, HillGiant.class, ColossalDreadmaw.class,
        ThreeTreeMascot.class, TreetopSentries.class, RustShieldRampager.class, CultivatorsCaravan.class})
class HuntersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the source creature's power when it enters")
    void entersDealsSourcePowerDamage() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        castTalent(source, victim);

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("At level 2, targets an attacking creature for a temporary power and trample boost")
    void levelTwoBoostsTargetAttacker() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("At level 3, draws at end of turn when you control a creature with power 4 or greater")
    void levelThreeDrawsWithLargeCreature() {
        Permanent source = addCreatureReady(player1, new GrizzlyBears());
        Permanent victim = addCreatureReady(player2, new HillGiant());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        addCreatureReady(player1, new ColossalDreadmaw());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void gainingClassLevelsDoesNotPutCountersOnTheClass() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);

        levelUp(talent, 0, 1);

        assertThat(talent.getClassLevel()).isEqualTo(2);
        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();

        levelUp(talent, 1, 3);

        assertThat(talent.getClassLevel()).isEqualTo(3);
        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();
    }

    @Test
    void enteringRequiresAnOpposingCreatureTargetWhenOneIsAvailable() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        harness.setHand(player1, List.of(new HuntersTalent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        PendingInteraction.PermanentChoice sourceChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(sourceChoice).isNotNull();
        assertThat(sourceChoice.validIds()).containsExactly(source.getId());
        harness.handlePermanentChosen(player1, source.getId());

        PendingInteraction.PermanentChoice victimChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(victimChoice).isNotNull();
        assertThat(victimChoice.validIds()).containsExactly(victim.getId());
        harness.handlePermanentChosen(player1, victim.getId());
        resolveAllTriggers();

        assertThat(victim.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void levelOneDoesNotTriggerWhenYouAttack() {
        Permanent attacker = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        castTalent(attacker, victim);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void levelTwoDoesNotCreateAnEndStepTriggerEvenWithFourPowerCreature() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        addCreatureReady(player1, new RustShieldRampager());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void levelThreeDoesNotTriggerWithoutYourOwnFourPowerCreature() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        addCreatureReady(player2, new RustShieldRampager());

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void endStepConditionIsCheckedAgainWhenTheTriggerResolves() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        Permanent largeCreature = addCreatureReady(player1, new RustShieldRampager());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThreeTreeMascot()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(largeCreature);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @CardUsed({HuntersTalent.class, ThreeTreeMascot.class, TreetopSentries.class, CultivatorsCaravan.class})
    void uncrewedVehicleDoesNotSatisfyTheCreaturePowerCondition() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        harness.addToBattlefield(player1, new CultivatorsCaravan());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThreeTreeMascot()));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void levelThreeDrawsWithExactlyFourPowerOnYourEndStepOnly() {
        Permanent source = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(source, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        addCreatureReady(player1, new RustShieldRampager());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new ThreeTreeMascot()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void levelThreeRetainsAttackAbilityAndBoostsOnlyOneAttacker() {
        Permanent attacker = addCreatureReady(player1, new ThreeTreeMascot());
        Permanent victim = addCreatureReady(player2, new TreetopSentries());
        Permanent talent = castTalent(attacker, victim);
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 3);
        Permanent otherAttacker = addCreatureReady(player1, new ThreeTreeMascot());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(otherAttacker)));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(attacker.getId(), otherAttacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, otherAttacker)).isEqualTo(2);
        assertThat(otherAttacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    private Permanent castTalent(Permanent source, Permanent victim) {
        harness.setHand(player1, List.of(new HuntersTalent()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, List.of(source.getId(), victim.getId()));
        resolveAllTriggers();
        return findPermanent(player1, "Hunter's Talent");
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        resolveAllTriggers();
    }
}
