package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelingMinister.class, GrizzlyBears.class, Plains.class})
class TravelingMinisterTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature and gains 1 life")
    void boostsCreatureAndGainsLife() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, targetId);
        assertThat(findPermanent(player1, "Traveling Minister").isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires a creature target")
    void requiresCreatureTarget() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, plains.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void requiresSorcerySpeed() {
        setupOnMyTurn(TurnStep.END_STEP);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void setupOnMyTurn(TurnStep step) {
        harness.addToBattlefield(player1, new TravelingMinister());
        harness.addToBattlefield(player1, new GrizzlyBears());
        findPermanent(player1, "Traveling Minister").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
