package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
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
        assertThat(token.isAttackedThisTurn()).isTrue();
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
}
