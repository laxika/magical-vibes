package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CombatCelebrant.class, DuneBeetle.class})
class CombatCelebrantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers the exert may prompt")
    void attackTriggersExertPrompt() {
        addCreatureReady(player1, new CombatCelebrant());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Exerting untaps other creatures, keeps the Celebrant exerted, and grants an extra combat phase")
    void exertUntapsOthersAndGrantsExtraCombat() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new DuneBeetle());
        otherCreature.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(otherCreature.isTapped()).isFalse();
        assertThat(celebrant.isTapped()).isTrue();
        assertThat(celebrant.getSkipUntapCount()).isGreaterThan(0);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Declining exert leaves other creatures tapped and grants no extra combat")
    void decliningExertDoesNothing() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new DuneBeetle());
        otherCreature.tap();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(celebrant.getSkipUntapCount()).isZero();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Exerting applies the untap restriction before its triggered ability resolves")
    void exertRestrictionAppliesBeforeTriggerResolves() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new DuneBeetle());
        otherCreature.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
        });

        assertThat(gd.stack).hasSize(1);
        assertThat(otherCreature.isTapped()).isTrue();
        assertThat(celebrant.getSkipUntapCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("An untapped Celebrant cannot be exerted again in a later combat of the same turn")
    void cannotExertTwiceInOneTurn() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        addCreatureReady(player1, new CombatCelebrant());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(1));
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });
        assertThat(celebrant.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("An exerted Celebrant skips only its controller's next untap step")
    void skipsOneUntapStep() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(celebrant.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(celebrant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An exerted Celebrant can untap in a different controller's untap step")
    void exertRestrictionDoesNotFollowNewController() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(celebrant);
        gd.playerBattlefields.get(player2.getId()).add(celebrant);

        harness.performUntapStep(player2);

        assertThat(celebrant.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Exert untaps your other attackers and nonattackers, but not opposing creatures")
    void untapsOnlyOtherControlledCreaturesWithoutRemovingAttackers() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherAttacker = addCreatureReady(player1, new DuneBeetle());
        Permanent nonattacker = addCreatureReady(player1, new DuneBeetle());
        Permanent opposingCreature = addCreatureReady(player2, new DuneBeetle());
        nonattacker.tap();
        opposingCreature.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        });

        assertThat(celebrant.isTapped()).isTrue();
        assertThat(otherAttacker.isTapped()).isFalse();
        assertThat(otherAttacker.isAttacking()).isTrue();
        assertThat(nonattacker.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The exert trigger still untaps other creatures and adds combat if its source leaves")
    void sourceLeavingDoesNotPreventExertBenefits() {
        Permanent celebrant = addCreatureReady(player1, new CombatCelebrant());
        Permanent otherCreature = addCreatureReady(player1, new DuneBeetle());
        otherCreature.tap();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);
            gd.playerBattlefields.get(player1.getId()).remove(celebrant);
            resolveAllTriggers();
        });

        assertThat(otherCreature.isTapped()).isFalse();
        harness.forceStep(TurnStep.END_OF_COMBAT);
        gs.advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);
    }
}
