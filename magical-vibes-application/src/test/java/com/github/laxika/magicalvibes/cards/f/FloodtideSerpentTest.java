package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NyxbornTriton;
import com.github.laxika.magicalvibes.cards.n.NyxbornWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FloodtideSerpent.class, GloriousAnthem.class, GrizzlyBears.class,
        Forest.class, FloodedWoodlands.class, NyxbornTriton.class, NyxbornWolf.class})
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

    @Test
    @DisplayName("The controller chooses which enchantment to return before paying the attack cost")
    void controllerChoosesEnchantmentToReturn() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new NyxbornTriton());
        harness.addToBattlefield(player1, new NyxbornWolf());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        harness.assertOnBattlefield(player1, "Nyxborn Wolf");
    }

    @Test
    @DisplayName("Two Serpents cannot attack by returning the same enchantment")
    void twoSerpentsRequireTwoEnchantments() {
        addCreatureReady(player1, new FloodtideSerpent());
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new NyxbornTriton());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0, 1)))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Nyxborn Triton");
    }

    @Test
    @DisplayName("Each attacking Serpent returns a separate enchantment")
    void twoSerpentsReturnTwoEnchantments() {
        addCreatureReady(player1, new FloodtideSerpent());
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new NyxbornTriton());
        harness.addToBattlefield(player1, new NyxbornTriton());

        declareAttackers(player1, List.of(0, 1));

        assertThat(countPermanents(player1, "Nyxborn Triton")).isZero();
        assertThat(gd.playerHands.get(player1.getId()).stream()
                .filter(card -> card.getName().equals("Nyxborn Triton"))).hasSize(2);
    }

    @Test
    @DisplayName("A tapped enchantment creature can pay the attack cost and returns to its owner")
    void returnsTappedEnchantmentCreatureToItsOwner() {
        addCreatureReady(player1, new FloodtideSerpent());
        NyxbornTriton triton = new NyxbornTriton();
        triton.setOwnerId(player2.getId());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, triton);
        enchantment.tap();

        declareAttackers(player1, List.of(0));

        harness.assertNotOnBattlefield(player1, "Nyxborn Triton");
        harness.assertInHand(player2, "Nyxborn Triton");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(triton);
    }

    @Test
    @DisplayName("Declining to attack does not return any enchantment")
    void notAttackingDoesNotPayReturnCost() {
        addCreatureReady(player1, new FloodtideSerpent());
        harness.addToBattlefield(player1, new NyxbornTriton());

        declareAttackers(player1, List.of());

        harness.assertOnBattlefield(player1, "Nyxborn Triton");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
