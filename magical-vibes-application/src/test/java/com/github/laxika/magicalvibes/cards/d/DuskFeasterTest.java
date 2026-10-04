package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorrowedHostility;
import com.github.laxika.magicalvibes.cards.c.CatharsShield;
import com.github.laxika.magicalvibes.cards.c.ChokingRestraints;
import com.github.laxika.magicalvibes.cards.l.LupinePrototype;
import com.github.laxika.magicalvibes.cards.w.WretchedGryff;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskFeaster.class, WretchedGryff.class, BorrowedHostility.class,
        ChokingRestraints.class, CatharsShield.class, LupinePrototype.class})
class DuskFeasterTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost without delirium")
    void canBeCastForFullCostWithoutDelirium() {
        harness.castFromHand(player1, new DuskFeaster(), "{5}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Delirium reduces the casting cost by {2}")
    void deliriumReducesCastingCost() {
        harness.setGraveyard(player1, List.of(
                new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints(), new CatharsShield()));

        harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot be cast for the reduced cost without delirium")
    void reducedCostRequiresDelirium() {
        assertThatThrownBy(() -> harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void artifactCreatureProvidesTwoTypesForDelirium() {
        harness.setGraveyard(player1, List.of(
                new LupinePrototype(), new BorrowedHostility(), new ChokingRestraints()));

        harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dusk Feaster");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void fourCardsWithOnlyThreeTypesDoNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(
                new WretchedGryff(), new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void opponentsGraveyardDoesNotEnableDelirium() {
        harness.setGraveyard(player1, List.of(new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints()));
        harness.setGraveyard(player2, List.of(
                new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints(), new CatharsShield()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void deliriumDoesNotReduceBlackManaRequirements() {
        harness.setGraveyard(player1, List.of(
                new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints(), new CatharsShield()));

        assertThatThrownBy(() -> harness.castFromHand(player1, new DuskFeaster(), "{4}{B}"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void deliriumReductionConsumesOnlyThreeGenericMana() {
        harness.setGraveyard(player1, List.of(
                new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints(), new CatharsShield()));

        harness.castFromHand(player1, new DuskFeaster(), "{5}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void losingDeliriumAfterCastingDoesNotPreventResolution() {
        harness.setGraveyard(player1, List.of(
                new WretchedGryff(), new BorrowedHostility(), new ChokingRestraints(), new CatharsShield()));
        harness.castFromHand(player1, new DuskFeaster(), "{3}{B}{B}");

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dusk Feaster");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new DuskFeaster());
        harness.addToBattlefield(player2, new LupinePrototype());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new DuskFeaster());
        harness.addToBattlefield(player2, new WretchedGryff());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Dusk Feaster");
        harness.assertInGraveyard(player2, "Wretched Gryff");
        harness.assertLife(player2, 20);
    }
}
