package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InsatiableSouleater;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpinedThopter.class, InsatiableSouleater.class})
class SpinedThopterTest extends BaseCardTest {

    @Test
    void castsWithBlueManaWithoutPayingLife() {
        harness.setHand(player1, List.of(new SpinedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spined Thopter");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void castsWithTwoGenericManaAndTwoLife() {
        harness.setHand(player1, List.of(new SpinedThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spined Thopter");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void canSpendBlueManaOnGenericCostAndPayLifeForPhyrexianSymbol() {
        harness.setHand(player1, List.of(new SpinedThopter()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spined Thopter");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotPayPhyrexianSymbolWithLessThanTwoLife() {
        harness.setHand(player1, List.of(new SpinedThopter()));
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        addCreatureReady(player1, new SpinedThopter());
        addCreatureReady(player2, new InsatiableSouleater());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void canBeBlockedByAnotherFlyingCreature() {
        addCreatureReady(player1, new SpinedThopter());
        addCreatureReady(player2, new SpinedThopter());
        declareAttackersAndPrepareBlockers(List.of(0));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Spined Thopter");
        harness.assertInGraveyard(player2, "Spined Thopter");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
