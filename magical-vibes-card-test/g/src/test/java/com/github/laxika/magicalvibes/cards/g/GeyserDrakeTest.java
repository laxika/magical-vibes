package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.f.FailedFording;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GeyserDrake.class, AmbushViper.class, FailedFording.class})
class GeyserDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Your spells cost {1} less to cast during another player's turn")
    void reducesYourSpellsDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GeyserDrake());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AmbushViper()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ambush Viper");
    }

    @Test
    @DisplayName("The cost reduction does not apply during your turn")
    void doesNotReduceYourSpellsDuringYourTurn() {
        harness.addToBattlefield(player1, new GeyserDrake());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AmbushViper()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesNoncreatureSpellsDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new GeyserDrake());
        var target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new FailedFording()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Geyser Drake");
        harness.assertNotOnBattlefield(player1, "Geyser Drake");
        harness.assertInGraveyard(player1, "Failed Fording");
    }

    @Test
    void doesNotReduceOpponentsSpellsDuringYourTurn() {
        harness.addToBattlefield(player1, new GeyserDrake());
        var target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new FailedFording()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Failed Fording");
    }

    @Test
    void multipleDrakesCannotReduceColoredMana() {
        harness.addToBattlefield(player1, new GeyserDrake());
        harness.addToBattlefield(player1, new GeyserDrake());
        var target = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FailedFording()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Failed Fording");
    }

    @Test
    void drakeInHandDoesNotReduceCastingCosts() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AmbushViper(), new GeyserDrake()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
