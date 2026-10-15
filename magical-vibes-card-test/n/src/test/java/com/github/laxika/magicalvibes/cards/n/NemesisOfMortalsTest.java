package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.s.SedgeScorpion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NemesisOfMortals.class, SedgeScorpion.class, NyleasPresence.class})
class NemesisOfMortalsTest extends BaseCardTest {

    @Test
    @DisplayName("Creature cards in the graveyard reduce Nemesis of Mortals's spell cost")
    void creatureCardsReduceSpellCost() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion()));
        harness.setHand(player1, List.of(new NemesisOfMortals()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Nemesis of Mortals's monstrosity cost is reduced by creature cards in its controller's graveyard")
    void creatureCardsReduceMonstrosityCost() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion()));
        Permanent nemesis = addReadyNemesis();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nemesis.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Nemesis of Mortals's monstrosity cost cannot be reduced below its colored mana cost")
    void monstrosityCostRetainsColoredManaRequirement() {
        harness.setGraveyard(player1, List.of(
                new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion(), new SedgeScorpion()));
        Permanent nemesis = addReadyNemesis();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("A monstrous Nemesis can activate monstrosity again but gains no more counters")
    void monstrosityOnlyResolvesOnce() {
        Permanent nemesis = addReadyNemesis();
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nemesis.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Spell cost reduction ignores noncreatures and the opponent's graveyard")
    void spellReductionCountsOnlyOwnCreatureCards() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new NyleasPresence()));
        harness.setGraveyard(player2, List.of(new SedgeScorpion(), new SedgeScorpion()));
        harness.setHand(player1, List.of(new NemesisOfMortals()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Monstrosity cost reduction ignores noncreatures and the opponent's graveyard")
    void activationReductionCountsOnlyOwnCreatureCards() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new NyleasPresence()));
        harness.setGraveyard(player2, List.of(new SedgeScorpion(), new SedgeScorpion()));
        Permanent nemesis = harness.addToBattlefieldAndReturn(player1, new NemesisOfMortals());
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nemesis.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Spell cost reduction retains both green mana symbols even with excess creatures")
    void spellCostRetainsColoredManaRequirement() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion()));
        harness.setHand(player1, List.of(new NemesisOfMortals()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Even maximum monstrosity reduction cannot replace required green mana")
    void activationRequiresBothGreenManaSymbols() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion(), new SedgeScorpion(), new SedgeScorpion()));
        Permanent nemesis = harness.addToBattlefieldAndReturn(player1, new NemesisOfMortals());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(nemesis.isMonstrous()).isFalse();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Changing the graveyard after activation does not change paid cost or monstrosity")
    void graveyardChangeAfterActivationDoesNotChangeResolution() {
        harness.setGraveyard(player1, List.of(new SedgeScorpion(), new SedgeScorpion(),
                new SedgeScorpion()));
        Permanent nemesis = harness.addToBattlefieldAndReturn(player1, new NemesisOfMortals());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nemesis.isMonstrous()).isTrue();
    }

    @Test
    @DisplayName("Two pending monstrosity activations only add five counters in total")
    void multiplePendingActivationsOnlyBecomeMonstrousOnce() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player1, new NemesisOfMortals());
        harness.addMana(player1, ManaColor.COLORLESS, 14);
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(2);
        assertThat(nemesis.isMonstrous()).isFalse();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(nemesis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(nemesis.isMonstrous()).isTrue();
    }

    private Permanent addReadyNemesis() {
        Permanent nemesis = harness.addToBattlefieldAndReturn(player1, new NemesisOfMortals());
        nemesis.setSummoningSick(false);
        return nemesis;
    }
}
