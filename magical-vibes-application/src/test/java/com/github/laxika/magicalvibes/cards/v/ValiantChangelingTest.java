package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValiantChangeling.class, AvianChangeling.class, GrizzlyBears.class, HillGiant.class, AmoeboidChangeling.class})
class ValiantChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast for its full cost without creatures")
    void canBeCastForFullCostWithoutCreatures() {
        prepareCast(5);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Costs {1} less for each distinct creature type you control")
    void costsLessForDistinctCreatureTypes() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        prepareCast(3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Duplicate types and opposing creatures do not increase the reduction")
    void duplicateAndOpposingTypesDoNotIncreaseReduction() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        prepareCast(4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Changeling counts as every creature type but the reduction is capped at five")
    void changelingCountsAsEveryCreatureTypeWithFiveManaCap() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.setHand(player1, List.of(new ValiantChangeling()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot pay the full generic cost with no creature types")
    void cannotCastWithoutEnoughGenericMana() {
        prepareCast(4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("A Valiant Changeling on the battlefield gives the maximum reduction")
    void existingValiantChangelingReducesTheNextOne() {
        harness.addToBattlefield(player1, new ValiantChangeling());
        prepareCast(0);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The maximum reduction still requires two white mana")
    void maximumReductionDoesNotReduceWhiteMana() {
        harness.addToBattlefield(player1, new ValiantChangeling());
        harness.setHand(player1, List.of(new ValiantChangeling()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Unblocked Valiant Changeling deals damage in both combat damage steps")
    void doubleStrikeDealsCombatDamageTwice() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new ValiantChangeling());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Changeling does not count as every type after losing all creature types")
    void lostCreatureTypesDoNotProvideCostReduction() {
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new ValiantChangeling());
        addCreatureReady(player2, new AmoeboidChangeling());
        harness.activateAbility(player2, 0, 1, null, changeling.getId());
        harness.passBothPriorities();
        prepareCast(0);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
    private void prepareCast(int colorlessMana) {
        harness.setHand(player1, List.of(new ValiantChangeling()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, colorlessMana);
    }
}
