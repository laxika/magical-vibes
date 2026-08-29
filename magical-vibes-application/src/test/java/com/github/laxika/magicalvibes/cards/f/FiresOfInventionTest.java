package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiresOfInvention.class, Forest.class, GrizzlyBears.class, Shock.class})
class FiresOfInventionTest extends BaseCardTest {

    @Test
    @DisplayName("Controller can cast a spell for free when its mana value is within the land count")
    void castsSpellForFreeWithinLandCount() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Spells above the land-count mana-value cap still require mana")
    void rejectsSpellAboveLandCountWithoutMana() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller can cast no more than two spells each turn")
    void limitsControllerToTwoSpells() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));

        harness.castInstant(player1, 0, player2.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller cannot cast spells during an opponent's turn")
    void controllerCannotCastDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Opponent can cast spells during their own turn")
    void opponentCanCastDuringOwnTurn() {
        harness.addToBattlefield(player1, new FiresOfInvention());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
    }
}
