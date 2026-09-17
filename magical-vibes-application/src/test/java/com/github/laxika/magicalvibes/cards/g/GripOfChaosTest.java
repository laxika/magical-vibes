package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.j.JoragaAuxiliary;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
import com.github.laxika.magicalvibes.cards.r.ReturnToDust;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GripOfChaos.class, Murder.class, PeelFromReality.class, ReturnToDust.class,
        FountainOfYouth.class, WyluliWolf.class, JoragaAuxiliary.class, GrizzlyBears.class})
class GripOfChaosTest extends BaseCardTest {

    @Test
    void reselectsSingleTargetSpellToAnotherLegalTarget() {
        harness.addToBattlefield(player1, new GripOfChaos());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, originalTarget.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(originalTarget.getId()));
        harness.passBothPriorities();

        StackEntry murder = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Murder"))
                .findFirst()
                .orElseThrow();
        assertThat(murder.getTargetId()).isEqualTo(alternateTarget.getId());
    }

    @Test
    void reselectsSingleTargetActivatedAbility() {
        harness.addToBattlefield(player1, new GripOfChaos());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var wyluliWolf = addCreatureReady(player2, new WyluliWolf());

        harness.activateAbility(player2, 0, null, originalTarget.getId());
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(originalTarget.getId()));
        harness.passBothPriorities();

        StackEntry ability = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Wyluli Wolf"))
                .findFirst()
                .orElseThrow();
        assertThat(ability.getTargetId()).isIn(alternateTarget.getId(), wyluliWolf.getId());
    }

    @Test
    void leavesTargetUnchangedWhenNoLegalReplacementExists() {
        harness.addToBattlefield(player1, new GripOfChaos());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, originalTarget.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getId().equals(originalTarget.getId())
                        || permanent.getId().equals(alternateTarget.getId()));
        harness.passBothPriorities();

        StackEntry murder = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Murder"))
                .findFirst()
                .orElseThrow();
        assertThat(murder.getTargetId()).isEqualTo(originalTarget.getId());

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reselectsSingleTargetStoredInTargetList() {
        var gripOfChaos = harness.addToBattlefieldAndReturn(player1, new GripOfChaos());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        var alternateTarget = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new ReturnToDust()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, List.of(originalTarget.getId()));

        StackEntry returnToDust = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Return to Dust"))
                .findFirst()
                .orElseThrow();
        assertThat(returnToDust.getTargetId()).isNull();
        assertThat(returnToDust.getTargetIds()).containsExactly(originalTarget.getId());

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getId().equals(gripOfChaos.getId()));
        gd.playerBattlefields.get(player2.getId()).removeIf(permanent ->
                permanent.getId().equals(originalTarget.getId()));
        harness.passBothPriorities();
        assertThat(returnToDust.getTargetId()).isEqualTo(alternateTarget.getId());
        assertThat(returnToDust.getTargetIds()).containsExactly(originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .contains(alternateTarget.getCard())
                .doesNotContain(originalTarget.getCard());
    }

    @Test
    void leavesSingleChosenTargetOfMultiTargetAbilityUnchangedWhenNoLegalReplacementExists() {
        harness.addToBattlefield(player1, new GripOfChaos());
        addCreatureReady(player1, new JoragaAuxiliary());
        var originalTarget = addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbilityWithMultiTargets(player1, 1, 0, List.of(originalTarget.getId()));

        gd.playerBattlefields.get(player1.getId()).removeIf(permanent ->
                permanent.getId().equals(originalTarget.getId()));
        harness.passBothPriorities();

        StackEntry ability = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Joraga Auxiliary"))
                .findFirst()
                .orElseThrow();
        assertThat(ability.getTargetId()).isNull();
        assertThat(ability.getTargetIds()).containsExactly(originalTarget.getId());
    }

    @Test
    void doesNotReselectSpellWithMultipleTargets() {
        harness.addToBattlefield(player1, new GripOfChaos());
        var ownTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var opposingTarget = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new PeelFromReality()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, List.of(ownTarget.getId(), opposingTarget.getId()));

        StackEntry peelFromReality = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Peel from Reality"))
                .findFirst()
                .orElseThrow();
        List<java.util.UUID> originalTargets = new ArrayList<>(peelFromReality.getTargetIds());

        harness.passBothPriorities();

        assertThat(peelFromReality.getTargetIds()).containsExactlyElementsOf(originalTargets);
    }
}
