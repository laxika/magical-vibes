package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SerraAvenger.class, VedalkenOrrery.class, SilvercoatLion.class})
class SerraAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Not castable during the controller's first turn")
    void notCastableOnFirstTurn() {
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Not castable during the controller's third turn")
    void notCastableOnThirdTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Castable during the controller's fourth turn")
    void castableOnFourthTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 4);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Avenger");
    }

    @Test
    @DisplayName("Only the caster's own turns count — an opponent's fourth turn does not unlock it")
    void opponentTurnCountDoesNotUnlock() {
        gd.turnsTakenByPlayer.put(player2.getId(), 4);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Not castable during the controller's second turn")
    void notCastableOnSecondTurn() {
        gd.turnsTakenByPlayer.put(player1.getId(), 2);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    @DisplayName("Flash permission does not override the restriction on the caster's first three turns")
    void flashDoesNotOverrideOwnTurnRestriction(int ownTurn) {
        gd.turnsTakenByPlayer.put(player1.getId(), ownTurn);
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    @DisplayName("With flash, castable on an opponent's turn before the caster's fourth turn")
    void castableOnOpponentsTurnWithFlash(int ownTurnsTaken) {
        gd.turnsTakenByPlayer.put(player1.getId(), ownTurnsTaken);
        gd.turnsTakenByPlayer.put(player2.getId(), Math.max(1, ownTurnsTaken));
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.ensurePriority(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Serra Avenger");
    }

    @Test
    @DisplayName("The opponent-turn exception does not itself grant flash")
    void cannotCastOnOpponentsTurnWithoutFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SerraAvenger()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.ensurePriority(player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Serra Avenger")
    void groundCreatureCannotBlock() {
        Permanent avenger = addCreatureReady(player1, new SerraAvenger());
        addCreatureReady(player2, new SilvercoatLion());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
        assertThat(avenger.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("A flying creature can block Serra Avenger")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new SerraAvenger());
        Permanent blocker = addCreatureReady(player2, new SerraAvenger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance keeps Serra Avenger untapped when attacking")
    void staysUntappedWhenAttacking() {
        Permanent avenger = addCreatureReady(player1, new SerraAvenger());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(avenger.isAttacking()).isTrue();
        assertThat(avenger.isTapped()).isFalse();
    }
}
