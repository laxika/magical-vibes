package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HarshDeceiver;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BreathOfTheSleepless.class, GrizzlyBears.class, HarshDeceiver.class, Shock.class})
class BreathOfTheSleeplessTest extends BaseCardTest {

    @Test
    @DisplayName("Spirit spells can be cast during an opponent's turn and trigger the tap ability")
    void spiritSpellDuringOpponentsTurnTapsUpToOneCreature() {
        harness.addToBattlefield(player1, new BreathOfTheSleepless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new HarshDeceiver()));
        addManaForHarshDeceiver();

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(bears.getId());
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The tap target is optional")
    void mayDeclineTapTarget() {
        harness.addToBattlefield(player1, new BreathOfTheSleepless());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new HarshDeceiver()));
        addManaForHarshDeceiver();

        harness.castCreature(player1, 0);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Noncreature spells do not trigger the tap ability")
    void noncreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new BreathOfTheSleepless());
        harness.addToBattlefield(player2, new GrizzlyBears());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Non-Spirit creature spells cannot use the granted flash permission")
    void nonSpiritCreatureCannotBeCastDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new BreathOfTheSleepless());
        enterOpponentsTurn();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void enterOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addManaForHarshDeceiver() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
