package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AssaultGriffin;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CircleOfFlame.class, RuneclawBear.class, AssaultGriffin.class, GarrukPrimalHunter.class})
class CircleOfFlameTest extends BaseCardTest {

    /** Puts Circle of Flame on player1's battlefield and the given attacker on player2's. */
    private Permanent setUpAttack(Card attackerCard) {
        harness.addToBattlefield(player1, new CircleOfFlame());
        return addCreatureReady(player2, attackerCard);
    }

    @Test
    @DisplayName("A non-flying attacker triggers Circle of Flame against that attacker")
    void nonFlyerTriggersAbility() {
        Permanent attacker = setUpAttack(new RuneclawBear());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving the trigger deals 1 damage to the attacking creature")
    void dealsOneDamageToAttacker() {
        Permanent attacker = setUpAttack(new RuneclawBear());

        declareAttackers(player2, List.of(0));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A flying attacker does not trigger Circle of Flame")
    void flyerDoesNotTrigger() {
        Permanent attacker = setUpAttack(new AssaultGriffin());

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An attacker attacking a controlled planeswalker also takes damage")
    void damagesAttackerAttackingPlaneswalker() {
        Permanent attacker = setUpAttack(new RuneclawBear());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GarrukPrimalHunter());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player2, List.of(0), Map.of(0, planeswalker.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gaining flying after triggering does not prevent damage")
    void damagesAttackerThatGainsFlyingAfterDeclaration() {
        Permanent attacker = setUpAttack(new RuneclawBear());
        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);

        attacker.getGrantedKeywords().add(Keyword.FLYING);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Shroud does not prevent the non-targeting damage ability")
    void damagesAttackerWithShroud() {
        Permanent attacker = setUpAttack(new RuneclawBear());
        attacker.getGrantedKeywords().add(Keyword.SHROUD);

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Each non-flying attacker triggers separately")
    void triggersForEachNonFlyingAttacker() {
        Permanent first = setUpAttack(new RuneclawBear());
        Permanent second = addCreatureReady(player2, new RuneclawBear());
        Permanent flyer = addCreatureReady(player2, new AssaultGriffin());

        declareAttackers(player2, List.of(0, 1, 2));

        assertThat(gd.stack).hasSize(2);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        assertThat(flyer.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Removing Circle of Flame does not stop its pending trigger")
    void pendingTriggerSurvivesSourceRemoval() {
        Permanent attacker = setUpAttack(new RuneclawBear());
        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent source = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }
}
