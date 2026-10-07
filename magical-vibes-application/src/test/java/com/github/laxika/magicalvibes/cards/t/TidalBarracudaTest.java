package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Cultivate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Tidal Barracuda")
@CardUsed({TidalBarracuda.class, GrizzlyBears.class, Shock.class, Cultivate.class, SolRing.class})
class TidalBarracudaTest extends BaseCardTest {

    @Test
    @DisplayName("The controller may cast creature spells at instant speed")
    void controllerMayCastSpellsAtInstantSpeed() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent may cast spells at instant speed on their own turn")
    void opponentMayCastSpellsAtInstantSpeedOnTheirTurn() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Opponents cannot cast spells during the controller's turn")
    void opponentsCannotCastDuringControllerTurn() {
        Permanent barracuda = harness.addToBattlefieldAndReturn(player1, new TidalBarracuda());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, barracuda.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The controller may cast a sorcery during an opponent's turn")
    void controllerMayCastSorceryDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new Cultivate()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player1, "Cultivate");
    }

    @Test
    @DisplayName("An opponent may cast an artifact outside their main phase on their own turn")
    void opponentMayCastArtifactOutsideMainPhase() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(new SolRing()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.assertNotInHand(player2, "Sol Ring");
    }

    @Test
    @DisplayName("Opponents can activate mana abilities during the controller's turn")
    void opponentMayActivateManaAbilityDuringControllerTurn() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        Permanent ring = harness.addToBattlefieldAndReturn(player2, new SolRing());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, null, null);

        assertThat(ring.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Barracuda's casting prohibition overrides flash permission")
    void opposingBarracudasDoNotLetOpponentCast() {
        harness.addToBattlefield(player1, new TidalBarracuda());
        harness.addToBattlefield(player2, new TidalBarracuda());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Grizzly Bears");
    }
}
