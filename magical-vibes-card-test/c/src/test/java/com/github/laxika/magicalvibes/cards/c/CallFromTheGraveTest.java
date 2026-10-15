package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({CallFromTheGrave.class, CanopySpider.class, DarkRitual.class, Clone.class})
class CallFromTheGraveTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a random creature and deals damage equal to its mana value")
    void returnsCreatureAndDealsManaValueDamage() {
        Card creature = new CanopySpider();
        harness.setGraveyard(player2, List.of(creature));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CallFromTheGrave(), "{2}{B}");
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
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CallFromTheGrave(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player2, "Dark Ritual");
    }

    @Test
    @DisplayName("Can return a creature from the caster's own graveyard")
    void returnsCreatureFromOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new CanopySpider(), new DarkRitual()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CallFromTheGrave(), "{2}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player1, "Canopy Spider");
        harness.assertInGraveyard(player1, "Dark Ritual");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chooses from the graveyards as they exist at resolution")
    void choosesCreatureAtResolution() {
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CallFromTheGrave(), "{2}{B}");
        harness.setGraveyard(player2, List.of(new CanopySpider()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player2, "Canopy Spider");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("A returned Clone copies before damage uses the resulting permanent's mana value")
    void usesManaValueOfCopiedPermanent() {
        harness.addToBattlefield(player2, new CanopySpider());
        harness.setGraveyard(player2, List.of(new Clone()));
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new CallFromTheGrave(), "{2}{B}");
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Canopy Spider"));

        harness.assertOnBattlefield(player1, "Canopy Spider");
        harness.assertNotInGraveyard(player2, "Clone");
        harness.assertLife(player1, 18);
    }
}
