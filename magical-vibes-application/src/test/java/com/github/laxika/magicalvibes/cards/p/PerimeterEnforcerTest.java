package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarketwatchPhantom;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PerimeterEnforcer.class, GrizzlyBears.class, MarketwatchPhantom.class, NoviceInspector.class})
class PerimeterEnforcerTest extends BaseCardTest {

    @Test
    void detectiveEnteringBoostsPerimeterEnforcerUntilEndOfTurn() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void nonDetectiveEnteringDoesNotTriggerPerimeterEnforcer() {
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(1);
    }

    @Test
    void detectiveTurnedFaceUpBoostsPerimeterEnforcer() {
        MarketwatchPhantom detective = new MarketwatchPhantom();
        detective.addMorph("{0}");
        harness.setHand(player1, List.of(detective));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent nightwatch = findPermanent(player1, "Marketwatch Phantom");
        Permanent enforcer = addCreatureReady(player1, new PerimeterEnforcer());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(nightwatch));
        resolveAllStack();

        assertThat(nightwatch.isFaceDown()).isFalse();
        assertThat(gqs.getEffectivePower(gd, enforcer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enforcer)).isEqualTo(2);
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
