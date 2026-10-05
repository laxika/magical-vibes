package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AlloyMyr;
import com.github.laxika.magicalvibes.cards.p.PriestOfUrabrask;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrSuperion.class, AlloyMyr.class, PriestOfUrabrask.class})
class MyrSuperionTest extends BaseCardTest {

    @Test
    @DisplayName("Myr Superion cannot be cast without mana")
    void cannotCastWithoutMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Myr Superion can be cast with creature-produced mana")
    void canCastWithCreatureMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.addCreatureMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Myr Superion cannot be cast with only land mana")
    void cannotCastWithLandMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr Superion cannot be cast with mixed land and insufficient creature mana")
    void cannotCastWithInsufficientCreatureMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.addCreatureMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr Superion can be cast by tapping creature mana dorks")
    void canCastByTappingCreatures() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        addCreatureReady(player1, new AlloyMyr());
        addCreatureReady(player1, new AlloyMyr());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureManaTotal()).isEqualTo(2);

        // Cast Myr Superion
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Myr Superion resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.addCreatureMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        assertThat(battlefield).hasSize(1);
        assertThat(battlefield.getFirst().getCard()).isInstanceOf(MyrSuperion.class);
    }

    @Test
    @DisplayName("Creature mana tracking across different colors works for Myr Superion")
    void creatureManaTrackedAcrossColors() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));

        // Add 1 green creature mana and 1 white creature mana
        harness.addCreatureMana(player1, ManaColor.GREEN, 1);
        harness.addCreatureMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Casting Myr Superion spends creature mana and preserves land mana")
    void spendsOnlyCreatureManaFromMixedPool() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion()));
        harness.addCreatureMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getCreatureManaTotal()).isZero();
    }

    @Test
    @DisplayName("Land mana left after casting Myr Superion cannot cast another")
    void cannotReuseCreatureManaForSecondCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MyrSuperion(), new MyrSuperion()));
        harness.addCreatureMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Myr Superion can use mana from a creature's enter trigger")
    void canCastWithTriggeredCreatureMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new PriestOfUrabrask(), new MyrSuperion()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Myr Superion");
    }

    @Test
    @DisplayName("Creature mana tracking is cleared when mana pool is cleared")
    void creatureManaIsCleared() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.GREEN);
        pool.addCreatureMana(ManaColor.GREEN, 1);

        pool.clear();

        assertThat(pool.getCreatureManaTotal()).isEqualTo(0);
        assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Spending non-creature mana preserves creature mana tracking")
    void spendingNonCreatureManaPreservesCreatureTracking() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // 3 green total: 2 from creatures, 1 from land
        pool.add(ManaColor.GREEN, 3);
        pool.addCreatureMana(ManaColor.GREEN, 2);

        // Spend 1 green — non-creature mana is spent first
        pool.remove(ManaColor.GREEN);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Spending forces creature mana reduction when no non-creature mana remains")
    void spendingForcesCreatureManaReduction() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // 2 green, all from creatures
        pool.add(ManaColor.GREEN, 2);
        pool.addCreatureMana(ManaColor.GREEN, 2);

        // Spend 1 green — must reduce creature mana
        pool.remove(ManaColor.GREEN);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getCreatureMana(ManaColor.GREEN)).isEqualTo(1);
    }
}
