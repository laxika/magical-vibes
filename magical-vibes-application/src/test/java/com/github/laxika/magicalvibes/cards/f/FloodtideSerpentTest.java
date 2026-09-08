package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FloodtideSerpentTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack without an enchantment to return")
    void cannotAttackWithoutEnchantment() {
        addCreatureReady(player1, new FloodtideSerpent());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returns a controlled enchantment to its owner's hand when attacking")
    void returnsEnchantmentToHandWhenAttacking() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new GloriousAnthem());

        declareAttackers(player1, List.of(0));

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
    }

    @Test
    @DisplayName("Pays the return cost after another attack cost removes earlier permanents")
    void paysReturnCostAfterAnotherAttackCostRemovesEarlierPermanents() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        Permanent firstGreenAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondGreenAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent serpent = addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new FloodedWoodlands());

        List<Permanent> attackers = List.of(firstGreenAttacker, secondGreenAttacker, serpent);
        List<Integer> attackerIndices = attackers.stream()
                .map(attacker -> gd.playerBattlefields.get(player1.getId()).indexOf(attacker))
                .toList();

        declareAttackers(player1, attackerIndices);

        harness.assertInHand(player1, "Glorious Anthem");
        harness.assertNotOnBattlefield(player1, "Glorious Anthem");
        assertThat(countPermanents(player1, "Forest")).isZero();
        harness.assertOnBattlefield(player1, "Floodtide Serpent");
    }

    @Test
    @DisplayName("Cannot use an enchantment controlled by an opponent")
    void cannotUseOpponentsEnchantment() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player2, new GloriousAnthem());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
