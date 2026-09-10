package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CanopySpider;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Reanimate.class, CanopySpider.class, DarkRitual.class})
class ReanimateTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature from own graveyard and loses life equal to its mana value")
    void reanimatesFromOwnGraveyard() {
        Card creature = new CanopySpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player1, "Canopy Spider");
        // Canopy Spider has mana value 2
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Returns a creature from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Card creature = new CanopySpider();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player2, "Canopy Spider");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in a graveyard")
    void cannotTargetNonCreature() {
        Card instant = new DarkRitual();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles with no life loss when the targeted card leaves the graveyard")
    void fizzlesWhenTargetLeavesGraveyard() {
        Card creature = new CanopySpider();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Reanimate()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Canopy Spider");
        harness.assertLife(player1, 20);
    }
}
