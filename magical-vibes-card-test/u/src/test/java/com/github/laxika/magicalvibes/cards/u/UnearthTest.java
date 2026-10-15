package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BloatedToad;
import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.o.Ostracize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unearth.class, BloatedToad.class, GiantCockroach.class, Ostracize.class})
class UnearthTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature card with mana value 3 or less from the graveyard")
    void returnsTargetLowManaValueCreature() {
        Card creature = new BloatedToad();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Bloated Toad");
        harness.assertNotInGraveyard(player1, "Bloated Toad");
        harness.assertInGraveyard(player1, "Unearth");
    }

    @Test
    @DisplayName("Cannot target a creature card with mana value greater than 3")
    void cannotTargetHighManaValueCreature() {
        Card creature = new GiantCockroach();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card with mana value 3 or less")
    void cannotTargetLowManaValueNoncreature() {
        Card noncreature = new Ostracize();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card creature = new BloatedToad();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Unearth and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Unearth()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unearth");
        harness.assertInHand(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Cycling discards as a cost and waits for resolution to draw")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Unearth()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Unearth");
        harness.assertNotInHand(player1, "Unearth");
        harness.assertNotInHand(player1, "Giant Cockroach");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Cycling requires two mana and leaves the card in hand when unpaid")
    void cannotCycleWithInsufficientMana() {
        harness.setHand(player1, List.of(new Unearth()));
        harness.setLibrary(player1, List.of(new GiantCockroach()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Unearth");
        harness.assertNotInGraveyard(player1, "Unearth");
        harness.assertNotInHand(player1, "Giant Cockroach");
    }

    @Test
    @DisplayName("Cannot cast Unearth without a target even with an eligible creature in the graveyard")
    void requiresTargetAtCasting() {
        harness.setGraveyard(player1, List.of(new BloatedToad()));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Unearth");
        harness.assertInGraveyard(player1, "Bloated Toad");
    }

    @Test
    @DisplayName("Does not return a replacement creature when the target leaves the graveyard")
    void doesNotChooseAnotherCreatureWhenTargetLeavesGraveyard() {
        Card target = new BloatedToad();
        Card other = new BloatedToad();
        harness.setGraveyard(player1, List.of(target, other));
        harness.setHand(player1, List.of(new Unearth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.setGraveyard(player1, List.of(other));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Bloated Toad");
        harness.assertInGraveyard(player1, "Bloated Toad");
        harness.assertInGraveyard(player1, "Unearth");
    }
}
