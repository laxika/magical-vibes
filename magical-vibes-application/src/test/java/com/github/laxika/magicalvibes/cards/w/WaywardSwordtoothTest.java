package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AxegrinderGiant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WaywardSwordtoothTest extends BaseCardTest {

    @Test
    @DisplayName("The controller gets one additional land play, but the opponent does not")
    void grantsAdditionalLandPlayToController() {
        harness.addToBattlefield(player1, new WaywardSwordtooth());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot attack without the city's blessing")
    void cannotAttackWithoutCityBlessing() {
        addCreatureReady(player1, new WaywardSwordtooth());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot block without the city's blessing")
    void cannotBlockWithoutCityBlessing() {
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new WaywardSwordtooth());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The city's blessing allows it to attack")
    void canAttackWithCityBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        addCreatureReady(player1, new WaywardSwordtooth());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
    }

    @Test
    @DisplayName("The city's blessing allows it to block")
    void canBlockWithCityBlessing() {
        gd.playersWithCityBlessing.add(player1.getId());
        addCreatureReady(player2, new AxegrinderGiant());
        addCreatureReady(player1, new WaywardSwordtooth());

        declareAttackers(player2, List.of(0));

        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Ascend grants the city's blessing when the tenth permanent enters")
    void ascendGrantsCityBlessing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new WaywardSwordtooth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
    }
}
