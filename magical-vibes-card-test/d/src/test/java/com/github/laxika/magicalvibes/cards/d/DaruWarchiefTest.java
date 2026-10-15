package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.n.NobleTemplar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaruWarchief.class, DaruSpiritualist.class, NobleTemplar.class})
class DaruWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Soldier creatures you control get +1/+2, including Daru Warchief")
    void boostsOwnSoldiers() {
        Permanent warchief = harness.addToBattlefieldAndReturn(player1, new DaruWarchief());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new NobleTemplar());

        assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(8);
    }

    @Test
    @DisplayName("Does not boost non-Soldiers or an opponent's Soldiers")
    void onlyBoostsOwnSoldiers() {
        harness.addToBattlefield(player1, new DaruWarchief());
        Permanent nonSoldier = harness.addToBattlefieldAndReturn(player1, new DaruSpiritualist());
        Permanent opponentSoldier = harness.addToBattlefieldAndReturn(player2, new NobleTemplar());

        assertThat(gqs.getEffectivePower(gd, nonSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, nonSoldier)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opponentSoldier)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentSoldier)).isEqualTo(6);
    }

    @Test
    @DisplayName("Soldier spells you cast cost {1} less to cast")
    void reducesOwnSoldierSpellCost() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player1, List.of(new NobleTemplar()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cost reduction does not apply to non-Soldier spells")
    void doesNotReduceNonSoldierSpellCost() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player1, List.of(new DaruSpiritualist()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cost reduction does not apply to an opponent's Soldier spells")
    void doesNotReduceOpponentSoldierSpellCost() {
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player2, List.of(new NobleTemplar()));
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.addMana(player2, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Multiple Warchiefs stack their bonuses and Soldier cost reductions")
    void multipleWarchiefsStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DaruWarchief());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DaruWarchief());
        Permanent soldier = harness.addToBattlefieldAndReturn(player1, new NobleTemplar());

        for (Permanent warchief : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, warchief)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, warchief)).isEqualTo(5);
        }
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(10);

        harness.setHand(player1, List.of(new NobleTemplar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Soldier cost reductions cannot pay colored mana requirements")
    void doesNotReduceColoredManaRequirements() {
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.addToBattlefield(player1, new DaruWarchief());
        harness.setHand(player1, List.of(new DaruWarchief()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A Warchief spell does not reduce its own casting cost")
    void doesNotReduceItsOwnCostFromHand() {
        harness.setHand(player1, List.of(new DaruWarchief()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }
}
