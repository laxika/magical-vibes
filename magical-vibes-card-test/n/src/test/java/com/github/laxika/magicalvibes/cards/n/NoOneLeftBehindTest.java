package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GixianSkullflayer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoOneLeftBehind.class, GrizzlyBears.class, AirElemental.class, GixianSkullflayer.class})
class NoOneLeftBehindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a qualifying creature from the graveyard for the reduced cost")
    void returnsLowManaValueCreatureForReducedCost() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does not reduce the cost for a creature with mana value greater than 3")
    void doesNotReduceCostForExpensiveCreature() {
        Card creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducesCostForManaValueExactlyThree() {
        Card creature = new GixianSkullflayer();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Gixian Skullflayer");
        harness.assertNotInGraveyard(player1, "Gixian Skullflayer");
        harness.assertInGraveyard(player1, "No One Left Behind");
    }

    @Test
    void returnsExpensiveCreatureForFullCost() {
        Card creature = new AirElemental();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Air Elemental");
        harness.assertNotInGraveyard(player1, "Air Elemental");
        harness.assertNotOnBattlefield(player2, "Air Elemental");
    }

    @Test
    void reducedCostStillRequiresOneGenericMana() {
        Card creature = new GixianSkullflayer();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reducedCostStillRequiresBlackMana() {
        Card creature = new GixianSkullflayer();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetOpponentsCreatureCard() {
        Card creature = new GixianSkullflayer();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetNoncreatureCard() {
        Card sorcery = new NoOneLeftBehind();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, sorcery.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new GixianSkullflayer();
        Card other = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new NoOneLeftBehind()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, target.getId());

        harness.setGraveyard(player1, List.of(other));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gixian Skullflayer");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "No One Left Behind");
    }
}
