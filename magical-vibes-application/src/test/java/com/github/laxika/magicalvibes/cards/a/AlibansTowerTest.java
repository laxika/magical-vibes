package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeathSpeakers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlibansTower.class, DeathSpeakers.class})
class AlibansTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Gives a blocking creature +3/+1 until end of turn")
    void boostsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player1);
        setupTower();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Aliban's Tower");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOff() {
        Permanent blocker = addBlockingCreature(player1);
        setupTower();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isEqualTo(0);
        assertThat(blocker.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a creature that is not blocking")
    void cannotTargetNonBlockingCreature() {
        addBlockingCreature(player1);
        Permanent bystander = addCreatureReady(player1, new DeathSpeakers());
        setupTower();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("blocking");
    }

    @Test
    @DisplayName("Can target a blocking creature controlled by an opponent")
    void boostsOpponentsBlockingCreature() {
        Permanent blocker = addBlockingCreature(player2);
        setupTower();

        harness.castAndResolveInstant(player1, 0, blocker.getId());

        assertThat(blocker.getPowerModifier()).isEqualTo(3);
        assertThat(blocker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Fizzles if the target stops blocking before resolution")
    void fizzlesIfTargetStopsBlockingBeforeResolution() {
        Permanent blocker = addBlockingCreature(player1);
        setupTower();

        harness.castInstant(player1, 0, blocker.getId());
        blocker.setBlocking(false);
        harness.passBothPriorities();

        assertThat(blocker.getPowerModifier()).isZero();
        assertThat(blocker.getToughnessModifier()).isZero();
    }

    private void setupTower() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new AlibansTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new DeathSpeakers());
        creature.setBlocking(true);
        return creature;
    }
}
