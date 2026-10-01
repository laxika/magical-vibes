package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InkDissolver;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StreamOfUnconsciousness.class, InkDissolver.class, PricklyBoggart.class})
class StreamOfUnconsciousnessTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature gets -4/-0 and controller draws when they control a Wizard")
    void debuffsAndDrawsWithWizard() {
        harness.addToBattlefield(player1, new InkDissolver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new StreamOfUnconsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size() - 1; // -1 for the spell leaving hand
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(0);

        // Controls a Wizard -> draws a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Target creature gets -4/-0 but controller draws nothing without a Wizard")
    void debuffsButNoDrawWithoutWizard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new StreamOfUnconsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size() - 1;
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(0);

        // No Wizard controlled -> no draw
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("An opponent's Wizard does not enable the draw")
    void opponentWizardDoesNotEnableDraw() {
        harness.addToBattlefield(player2, new InkDissolver());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new StreamOfUnconsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size() - 1;
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Can target a creature its controller owns")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new InkDissolver());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PricklyBoggart());
        harness.setHand(player1, List.of(new StreamOfUnconsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Debuff wears off at cleanup step")
    void debuffWearsOffAtCleanup() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PricklyBoggart());
        harness.setHand(player1, List.of(new StreamOfUnconsciousness()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
    }
}
