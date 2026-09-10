package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.p.PeelFromReality;
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

@CardUsed({GripOfChaos.class, Murder.class, PeelFromReality.class, WyluliWolf.class,
        GrizzlyBears.class})
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
        var wyluliWolf = harness.addToBattlefieldAndReturn(player2, new WyluliWolf());
        wyluliWolf.setSummoningSick(false);

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
