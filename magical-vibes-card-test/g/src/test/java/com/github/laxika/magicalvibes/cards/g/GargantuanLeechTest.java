package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HiddenNecropolis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GargantuanLeech.class, HiddenNecropolis.class, Plains.class})
class GargantuanLeechTest extends BaseCardTest {

    @Test
    void costsFullAmountWithoutCaves() {
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costsOneLessForEachControlledCaveAndCaveCardInGraveyard() {
        harness.addToBattlefield(player1, new HiddenNecropolis());
        harness.setGraveyard(player1, List.of(new HiddenNecropolis()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotCountOpponentsCavesOrNonCaveCardsInYourGraveyard() {
        harness.addToBattlefield(player2, new HiddenNecropolis());
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void countsMultipleControlledCavesWithoutGraveyardCards() {
        harness.addToBattlefield(player1, new HiddenNecropolis());
        harness.addToBattlefield(player1, new HiddenNecropolis());
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void countsMultipleCaveCardsInGraveyardWithoutControlledCaves() {
        harness.setGraveyard(player1, List.of(new HiddenNecropolis(), new HiddenNecropolis()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void doesNotCountOpponentsGraveyardOrCavesInHand() {
        harness.setGraveyard(player2, List.of(new HiddenNecropolis()));
        harness.setHand(player1, List.of(new GargantuanLeech(), new HiddenNecropolis()));
        harness.addToBattlefield(player1, new Plains());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void excessCavesReduceOnlyGenericMana() {
        for (int i = 0; i < 8; i++) {
            harness.addToBattlefield(player1, new HiddenNecropolis());
        }
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void excessCavesCannotPayTheBlackManaRequirement() {
        harness.setGraveyard(player1, List.of(
                new HiddenNecropolis(), new HiddenNecropolis(), new HiddenNecropolis(),
                new HiddenNecropolis(), new HiddenNecropolis(), new HiddenNecropolis(),
                new HiddenNecropolis(), new HiddenNecropolis()));
        harness.setHand(player1, List.of(new GargantuanLeech()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void lifelinkGainsLifeFromCombatDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent leech = addCreatureReady(player1, new GargantuanLeech());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(leech)));
        resolveCombat();

        harness.assertLife(player1, 25);
        harness.assertLife(player2, 15);
    }
}
