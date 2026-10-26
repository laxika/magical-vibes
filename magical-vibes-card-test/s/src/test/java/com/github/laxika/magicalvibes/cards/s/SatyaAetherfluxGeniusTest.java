package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.ParallelLives;
import com.github.laxika.magicalvibes.cards.q.QueenAllenalOfRuadach;
import com.github.laxika.magicalvibes.cards.u.UginTheSpiritDragon;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SatyaAetherfluxGenius.class, GrizzlyBears.class, Ornithopter.class})
class SatyaAetherfluxGeniusTest extends BaseCardTest {

    @Test
    void attacksCreatesTappedAttackingCopyAndTwoEnergy() {
        Permanent satya = addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttackedThisTurn()).isFalse();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, bears));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, bears));
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
        assertThat(satya.isAttackedThisTurn()).isTrue();
    }

    @Test
    void paysManaValueInEnergyToKeepCopy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });
        Permanent token = findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();

        advanceToEndStep();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    void declinesOptionalTargetButStillGetsEnergy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    void declinesEnergyPaymentAndSacrificesCopy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        advanceToEndStep();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }

    @Test
    void delayedSacrificeTriggersAtOpponentsEndStepIfOwnEndStepWasSkipped() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .containsExactly(bears);
    }

    @Test
    void insufficientEnergySacrificesCopyWithoutSpendingRemainingEnergy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });
        gd.playerEnergyCounters.put(player1.getId(), 1);

        advanceToEndStep();

        assertThat(findPermanents(player1, "Grizzly Bears")).containsExactly(bears);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void illegalOnlyTargetPreventsBothCopyAndEnergy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            gd.playerBattlefields.get(player1.getId()).remove(bears);
            gd.playerGraveyards.get(player1.getId()).add(bears.getCard());
            resolveAllTriggers();
        });

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    void canDeclineZeroEnergyPaymentToSacrificeZeroManaValueCopy() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent ornithopter = addCreatureReady(player1, new Ornithopter());
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, ornithopter.getId());
            resolveAllTriggers();
        });

        advanceToEndStep();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(findPermanents(player1, "Ornithopter")).containsExactly(ornithopter);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @CardUsed({UginTheSpiritDragon.class})
    @DisplayName("The copy may attack a planeswalker while Satya attacks its controller")
    void copyChoosesItsOwnAttackTarget() {
        Permanent satya = addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent ugin = harness.addToBattlefieldAndReturn(player2, new UginTheSpiritDragon());
        ugin.setCounterCount(CounterType.LOYALTY, 7);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
            assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
            harness.handlePermanentChosen(player1, ugin.getId());
            resolveAllTriggers();
        });

        assertThat(satya.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(bearTokens()).singleElement().satisfies(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(ugin.getId());
        });
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("With Queen Allenal and Parallel Lives, Satya's controller orders them before the copy enters")
    void asksToOrderTokenReplacements() {
        attackUnderQueenAndParallelLives();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.context()).isInstanceOf(ChoiceContext.ReplacementEffectOrder.class);
        assertThat(bearTokens()).isEmpty();
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("Applying Queen Allenal first doubles the copy and the Soldier, all attacking and all at risk")
    void queenFirstDoublesCopyAndSoldier() {
        attackUnderQueenAndParallelLives();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            chooseReplacement("Queen Allenal of Ruadach");
            resolveAllTriggers();
        });

        assertThat(bearTokens()).hasSize(2).allSatisfy(token -> {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(findPermanents(player1, "Soldier")).hasSize(2).allSatisfy(token -> {
            assertThat(token.isAttacking()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.getDelayedActions(SacrificePermanentAtControllerEndStepUnlessPays.class)).hasSize(4);
    }

    @Test
    @CardUsed({QueenAllenalOfRuadach.class, ParallelLives.class})
    @DisplayName("Applying Parallel Lives first adds one undoubled Soldier")
    void parallelLivesFirstAddsOneSoldier() {
        attackUnderQueenAndParallelLives();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            chooseReplacement("Parallel Lives");
            resolveAllTriggers();
        });

        assertThat(bearTokens()).hasSize(2);
        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(gd.getDelayedActions(SacrificePermanentAtControllerEndStepUnlessPays.class)).hasSize(3);
    }

    private void attackUnderQueenAndParallelLives() {
        addCreatureReady(player1, new SatyaAetherfluxGenius());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new QueenAllenalOfRuadach());
        harness.addToBattlefield(player1, new ParallelLives());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handlePermanentChosen(player1, bears.getId());
            resolveAllTriggers();
        });
    }

    private List<Permanent> bearTokens() {
        return findPermanents(player1, "Grizzly Bears").stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
    }

    private void chooseReplacement(String optionPrefix) {
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, choice.options().stream()
                .filter(option -> option.startsWith(optionPrefix)).findFirst().orElseThrow());
    }
}
