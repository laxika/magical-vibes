package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AnnihilatingFire;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.cards.m.Mulldrifter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakdosLordOfRiots.class, DrudgeBeetle.class, AnnihilatingFire.class,
        RubblebackRhino.class, Mulldrifter.class})
class RakdosLordOfRiotsTest extends BaseCardTest {

    private void addRakdosMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
    }

    @Test
    @DisplayName("Not castable when no opponent has lost life this turn")
    void notCastableWithoutOpponentLifeLoss() {
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Castable after an opponent lost life this turn")
    void castableAfterOpponentLifeLoss() {
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakdos, Lord of Riots");
    }

    @Test
    @DisplayName("Controller's own life loss does not enable the cast")
    void ownLifeLossDoesNotEnableCast() {
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new RakdosLordOfRiots()));
        addRakdosMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Creature spells cost {1} less per life opponents lost this turn")
    void reducesCreatureSpellCost() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        // Drudge Beetle costs {1}{G} — with {1} reduction it costs just {G}
        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Drudge Beetle");
    }

    @Test
    @DisplayName("Without opponent life loss, creature spells are not reduced")
    void noReductionWithoutLifeLoss() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Noncreature spells are not reduced")
    void noncreatureSpellsNotReduced() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 5);
        harness.setHand(player1, List.of(new AnnihilatingFire()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Controller's own life loss does not reduce creature costs")
    void ownLifeLossDoesNotReduce() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageToOpponentEnablesCastingRakdos() {
        harness.setHand(player1, List.of(new AnnihilatingFire(), new RakdosLordOfRiots()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 17);
        addRakdosMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rakdos, Lord of Riots");
    }

    @Test
    void reductionCountsEveryLifeLostBeforeRakdosEntered() {
        gd.lifeLostThisTurn.put(player2.getId(), 3);
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        harness.setHand(player1, List.of(new RubblebackRhino()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rubbleback Rhino");
    }

    @Test
    void excessiveReductionStillRequiresColoredMana() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 10);
        harness.setHand(player1, List.of(new DrudgeBeetle()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Drudge Beetle");
    }

    @Test
    void reductionAppliesToEveryCreatureSpell() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 1);
        harness.setHand(player1, List.of(new DrudgeBeetle(), new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Drudge Beetle"))
                .hasSize(2);
    }

    @Test
    void opponentsCreatureSpellsAreNotReduced() {
        harness.addToBattlefield(player2, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player1.getId(), 5);
        harness.setHand(player1, List.of(new DrudgeBeetle()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionAppliesToEvokeAlternativeCost() {
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        gd.lifeLostThisTurn.put(player2.getId(), 2);
        harness.setHand(player1, List.of(new Mulldrifter()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreatureWithEvoke(player1, 0, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mulldrifter");
    }

    @Test
    void subsequentLifeGainDoesNotEraseLifeLostForReduction() {
        harness.setHand(player1, List.of(new AnnihilatingFire(), new RubblebackRhino()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 10));
        harness.assertLife(player2, 27);
        harness.addToBattlefield(player1, new RakdosLordOfRiots());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rubbleback Rhino");
    }
}
