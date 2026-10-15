package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.u.UnholyOfficiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TravelingMinister.class, UnholyOfficiant.class, Plains.class, Abrade.class})
class TravelingMinisterTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature and gains 1 life")
    void boostsCreatureAndGainsLife() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Unholy Officiant");
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, targetId);
        assertThat(findPermanent(player1, "Traveling Minister").isTapped()).isTrue();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Unholy Officiant");
        assertThat(bears.getPowerModifier()).isEqualTo(1);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Unholy Officiant");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Unholy Officiant");
        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player2, new UnholyOfficiant());
        UUID targetId = harness.getPermanentId(player2, "Unholy Officiant");
        int controllerLife = gd.getLife(player1.getId());
        int opponentLife = gd.getLife(player2.getId());

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Unholy Officiant").getPowerModifier()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(controllerLife + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLife);
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
        UUID targetId = harness.getPermanentId(player1, "Unholy Officiant");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Can target itself during the postcombat main phase")
    void canTargetItself() {
        setupOnMyTurn(TurnStep.POSTCOMBAT_MAIN);
        Permanent minister = findPermanent(player1, "Traveling Minister");
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, minister.getId());
        harness.passBothPriorities();

        assertThat(minister.getPowerModifier()).isEqualTo(1);
        assertThat(minister.getToughnessModifier()).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Does not gain life when its only target leaves before resolution")
    void noLifeGainWhenTargetLeaves() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Unholy Officiant");
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, targetId);
        castAbradeInResponse(targetId);
        harness.assertInGraveyard(player1, "Unholy Officiant");
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves normally after the Minister leaves the battlefield")
    void resolvesAfterSourceLeaves() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        Permanent minister = findPermanent(player1, "Traveling Minister");
        Permanent target = findPermanent(player1, "Unholy Officiant");
        int startingLife = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        castAbradeInResponse(minister.getId());
        harness.assertInGraveyard(player1, "Traveling Minister");
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's main phase")
    void cannotActivateOnOpponentsTurn() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Unholy Officiant")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Cannot activate with a spell on the stack")
    void cannotActivateWithNonemptyStack() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        UUID targetId = harness.getPermanentId(player1, "Unholy Officiant");
        harness.setHand(player1, List.of(new Abrade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, 0, targetId);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    @DisplayName("Cannot pay the tap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        findPermanent(player1, "Traveling Minister").setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Unholy Officiant")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot pay the tap cost while already tapped")
    void cannotActivateWhileTapped() {
        setupOnMyTurn(TurnStep.PRECOMBAT_MAIN);
        findPermanent(player1, "Traveling Minister").tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                harness.getPermanentId(player1, "Unholy Officiant")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    private void castAbradeInResponse(UUID targetId) {
        harness.setHand(player2, List.of(new Abrade()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, 0, targetId);
        harness.passBothPriorities();
    }

    private void setupOnMyTurn(TurnStep step) {
        addCreatureReady(player1, new TravelingMinister());
        harness.addToBattlefield(player1, new UnholyOfficiant());
        harness.forceActivePlayer(player1);
        harness.forceStep(step);
    }
}
