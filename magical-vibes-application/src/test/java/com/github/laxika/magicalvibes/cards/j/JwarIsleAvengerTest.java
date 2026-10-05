package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JwarIsleAvenger.class, GrizzlyBears.class})
class JwarIsleAvengerTest extends BaseCardTest {

    @Test
    @DisplayName("Surge casts for {2}{U} after another spell was cast this turn")
    void surgeUsesAlternateCost() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new JwarIsleAvenger()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jwar Isle Avenger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Surge is unavailable before another spell is cast")
    void surgeRequiresAnotherSpellThisTurn() {
        harness.setHand(player1, List.of(new JwarIsleAvenger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The normal cost can be paid without enabling surge")
    void normalCostDoesNotRequireAnotherSpell() {
        harness.castFromHand(player1, new JwarIsleAvenger(), "{4}{U}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jwar Isle Avenger");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The normal cost remains available when surge is enabled")
    void normalCostRemainsOptionalAfterAnotherSpell() {
        harness.castFromHand(player1, new JwarIsleAvenger(), "{4}{U}");
        harness.passBothPriorities();

        harness.castFromHand(player1, new JwarIsleAvenger(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Jwar Isle Avenger")).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("An opponent's spell does not enable surge")
    void opponentSpellDoesNotEnableSurge() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new JwarIsleAvenger(), "{4}{U}");
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new JwarIsleAvenger()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("A creature without flying or reach cannot block Jwar Isle Avenger")
    void flyingPreventsGroundBlocker() {
        addCreatureReady(player1, new JwarIsleAvenger());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
