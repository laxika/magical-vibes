package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineFierceFighter.class, GrizzlyBears.class, Shock.class})
class WolverineFierceFighterTest extends BaseCardTest {

    @Test
    void entersAndFightsUpToOneOtherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WolverineFierceFighter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent wolverine = findPermanent(player1, "Wolverine, Fierce Fighter");
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void entersWithoutAValidFightTarget() {
        harness.setHand(player1, List.of(new WolverineFierceFighter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Wolverine, Fierce Fighter");
    }

    @Test
    void healsPreviouslyMarkedDamageBeforeRecordingNoncombatDamage() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player2, new WolverineFierceFighter());
        wolverine.setMarkedDamage(3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, wolverine.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wolverine);
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
        assertThat(wolverine.isDamagedByDeathtouch()).isFalse();
    }

    @Test
    void healsPreviouslyMarkedDamageBeforeRecordingCombatDamage() {
        Permanent wolverine = addCreatureReady(player1, new WolverineFierceFighter());
        wolverine.setMarkedDamage(3);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        wolverine.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolverine);
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
    }
}
