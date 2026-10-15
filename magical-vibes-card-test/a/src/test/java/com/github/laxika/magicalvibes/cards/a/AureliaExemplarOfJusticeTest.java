package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SavannahLions;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AureliaExemplarOfJustice.class, FugitiveWizard.class, GiantGrowth.class,
        GoblinPiker.class, GrizzlyBears.class, SavannahLions.class})
class AureliaExemplarOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Mentor targets only an attacking creature with lesser power")
    void mentorTargetsAttackingCreatureWithLesserPower() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent attackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent nonAttackingWizard = addCreatureReady(player1, new FugitiveWizard());
        Permanent equalPowerCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 3));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attackingWizard.getId());

        harness.handlePermanentChosen(player1, attackingWizard.getId());
        resolveAllTriggers();

        assertThat(attackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAttackingWizard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(equalPowerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Beginning of combat gives a red creature +2/+0 and trample")
    void beginningOfCombatBoostsRedCreature() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent target = addCreatureReady(player1, new GoblinPiker());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat gives a white creature +2/+0 and vigilance")
    void beginningOfCombatBoostsWhiteCreature() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent target = addCreatureReady(player1, new SavannahLions());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Beginning of combat can be declined and targets only creatures you control")
    void beginningOfCombatTargetIsOptionalAndControlled() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombat(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Aurelia can target herself and receives both keywords only until end of turn")
    void beginningOfCombatCanBoostSelfUntilEndOfTurn() {
        Permanent aurelia = addCreatureReady(player1, new AureliaExemplarOfJustice());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, aurelia.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aurelia)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aurelia)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.VIGILANCE)).isTrue();

        if (gd.interaction.activeInteraction() instanceof com.github.laxika.magicalvibes.model.PendingInteraction.AttackerDeclaration) {
            gs.declareAttackers(gd, player1, List.of());
        }
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, aurelia)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A creature that is neither red nor white still gets the power boost")
    void beginningOfCombatBoostsGreenCreatureWithoutKeywords() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Aurelia does not trigger at the beginning of the opponent's combat")
    void beginningOfCombatDoesNotTriggerForOpponent() {
        Permanent aurelia = addCreatureReady(player1, new AureliaExemplarOfJustice());

        advanceToCombat(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, aurelia)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, aurelia, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Mentor adds no counter if the target's power becomes too large before resolution")
    void mentorRechecksPowerAtResolution() {
        addCreatureReady(player1, new AureliaExemplarOfJustice());
        Permanent target = addCreatureReady(player1, new FugitiveWizard());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handlePermanentChosen(player1, target.getId());
            harness.castInstant(player1, 0, target.getId());
            resolveAllTriggers();
        });

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
