package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CallFromTheGrave.class, CanopySpider.class, DarkRitual.class})
class CallFromTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a random creature and deals damage equal to its mana value")
    void returnsCreatureAndDealsManaValueDamage() {
        Card creature = new CanopySpider();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new CallFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player2, "Canopy Spider");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Does nothing when no graveyard contains a creature")
    void doesNothingWithoutCreatureCards() {
        Card nonCreature = new DarkRitual();
        harness.setGraveyard(player2, List.of(nonCreature));
        harness.setHand(player1, List.of(new CallFromTheGrave()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Dark Ritual");
    }
}
