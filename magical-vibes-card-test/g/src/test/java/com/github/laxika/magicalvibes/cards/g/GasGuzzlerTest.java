package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GasGuzzler.class, GrizzlyBears.class, DuskLegionDreadnought.class, Forest.class})
class GasGuzzlerTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new GasGuzzler(), "{B}");
        harness.passBothPriorities();

        Permanent gasGuzzler = findPermanent(player1, "Gas Guzzler");
        assertThat(gasGuzzler.isTapped()).isTrue();
    }

    @Test
    void atMaxSpeedSacrificesAnotherCreatureAndDraws() {
        Permanent gasGuzzler = harness.addToBattlefieldAndReturn(player1, new GasGuzzler());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.passBothPriorities();

        assertThat(gasGuzzler).isIn(gd.playerBattlefields.get(player1.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canSacrificeAVehicle() {
        harness.addToBattlefield(player1, new GasGuzzler());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(vehicle);
        harness.assertInGraveyard(player1, "Dusk Legion Dreadnought");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void cannotActivateBelowMaxSpeed() {
        Permanent gasGuzzler = harness.addToBattlefieldAndReturn(player1, new GasGuzzler());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerSpeeds.put(player1.getId(), 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("max speed");
        assertThat(gasGuzzler).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(creature).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void enteringStartsSpeedWithoutResettingExistingSpeed() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new GasGuzzler(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(1);
        assertThat(gd.playerSpeeds.getOrDefault(player2.getId(), 0)).isZero();

        gd.playerSpeeds.put(player1.getId(), 3);
        harness.castFromHand(player1, new GasGuzzler(), "{B}");
        harness.passBothPriorities();

        assertThat(gd.playerSpeeds.get(player1.getId())).isEqualTo(3);
    }

    @Test
    void cannotSacrificeItselfOrAnOpponentsCreature() {
        Permanent gasGuzzler = harness.addToBattlefieldAndReturn(player1, new GasGuzzler());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GasGuzzler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(gasGuzzler).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(opponentCreature).isIn(gd.playerBattlefields.get(player2.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeALand() {
        harness.addToBattlefield(player1, new GasGuzzler());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);
        gd.playerSpeeds.put(player1.getId(), 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
        assertThat(land).isIn(gd.playerBattlefields.get(player1.getId()));
        assertThat(gd.stack).isEmpty();
    }
}
