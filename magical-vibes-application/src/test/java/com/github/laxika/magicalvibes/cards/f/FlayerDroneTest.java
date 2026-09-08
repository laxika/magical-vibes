package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlayerDrone.class, Ornithopter.class, GrizzlyBears.class})
class FlayerDroneTest extends BaseCardTest {

    @Test
    @DisplayName("A colorless creature entering makes a target opponent lose 1 life")
    void colorlessCreatureEnteringMakesTargetOpponentLoseLife() {
        harness.addToBattlefield(player1, new FlayerDrone());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("The trigger cannot target its controller")
    void triggerCannotTargetItsController() {
        harness.addToBattlefield(player1, new FlayerDrone());
        harness.setHand(player1, List.of(new Ornithopter()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A colored creature entering does not trigger Flayer Drone")
    void coloredCreatureEnteringDoesNotTrigger() {
        harness.addToBattlefield(player1, new FlayerDrone());
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Flayer Drone does not trigger for itself entering")
    void doesNotTriggerForItselfEntering() {
        int lifeBefore = gd.getLife(player2.getId());
        harness.setHand(player1, List.of(new FlayerDrone()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore);
    }
}
