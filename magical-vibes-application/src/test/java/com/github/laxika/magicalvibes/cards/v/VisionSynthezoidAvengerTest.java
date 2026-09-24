package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionSynthezoidAvenger.class, Shock.class})
class VisionSynthezoidAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("A non-active player casting a spell can put a +1/+1 counter on Vision")
    void nonActiveCasterCanChooseCounterMode() {
        Permanent vision = addVision();
        castShockAsNonActivePlayer();

        harness.handleListChoice(player1, "Put a +1/+1 counter on Vision.");
        harness.passBothPriorities();

        assertThat(vision.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Vision can phase out from the spell-cast trigger")
    void canChoosePhaseOutMode() {
        Permanent vision = addVision();
        castShockAsNonActivePlayer();

        harness.handleListChoice(player1, "Vision phases out.");
        harness.passBothPriorities();

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(vision);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vision);
    }

    @Test
    @DisplayName("A player casting during their own turn does not trigger Vision")
    void activeCasterDoesNotTrigger() {
        harness.addToBattlefield(player1, new VisionSynthezoidAvenger());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.pendingInteractions).isEmpty();
    }

    private Permanent addVision() {
        return harness.addToBattlefieldAndReturn(player1, new VisionSynthezoidAvenger());
    }

    private void castShockAsNonActivePlayer() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
    }
}
