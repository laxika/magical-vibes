package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LodestoneGolem;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecurityRhox.class, LodestoneGolem.class})
class SecurityRhoxTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for {R}{G} using mana produced by Treasures")
    void castsForTreasureMana() {
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.GREEN);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Cannot pay the alternate cost with ordinary mana")
    void cannotCastForOrdinaryMana() {
        harness.setHand(player1, List.of(new SecurityRhox()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot mix Treasure mana with ordinary mana for the alternate cost")
    void cannotMixTreasureAndOrdinaryMana() {
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can pay the normal cost using ordinary mana")
    void castsForNormalManaCost() {
        harness.castFromHand(player1, new SecurityRhox(), "{2}{R}{G}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Security Rhox");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Treasure mana must still provide both required colors")
    void cannotCastWithOnlyRedTreasureMana() {
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.RED);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Alternate casting spends Treasure mana and leaves ordinary mana available")
    void leavesOrdinaryManaUnspent() {
        harness.setHand(player1, List.of(new SecurityRhox()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.GREEN);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getTotalAllMana()).isEqualTo(2);
        assertThat(pool.getTreasureManaTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Security Rhox");
    }

    @Test
    @DisplayName("Lodestone Golem's cost increase applies to the alternate cost")
    void cannotIgnoreGenericCostIncrease() {
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.GREEN);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The increased alternate cost must also be paid entirely with Treasure mana")
    void cannotPayGenericCostIncreaseWithOrdinaryMana() {
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.GREEN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreatureWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Casting with a cost increase consumes the extra Treasure mana")
    void paysGenericCostIncreaseWithTreasureMana() {
        harness.addToBattlefield(player2, new LodestoneGolem());
        harness.setHand(player1, List.of(new SecurityRhox()));
        addTreasureMana(player1, ManaColor.RED);
        addTreasureMana(player1, ManaColor.GREEN);
        addTreasureMana(player1, ManaColor.BLUE);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Security Rhox");
    }

    private void addTreasureMana(com.github.laxika.magicalvibes.model.Player player, ManaColor color) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(color);
        pool.addTreasureManaTag(color, 1);
    }
}
