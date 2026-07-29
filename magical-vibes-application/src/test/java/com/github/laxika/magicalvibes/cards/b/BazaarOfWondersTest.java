package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BazaarOfWondersTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield exiles all graveyards")
    void enteringExilesAllGraveyards() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new Ornithopter()));

        harness.setHand(player1, List.of(new BazaarOfWonders()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities(); // enchantment resolves
        harness.passBothPriorities(); // ETB trigger resolves

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A spell is countered when a card with the same name is in a graveyard")
    void countersSpellWithSameNameInGraveyard() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // Bazaar trigger resolves — counters the spell
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A spell is countered when a nontoken permanent with the same name is on the battlefield")
    void countersSpellWithSameNameOnBattlefield() {
        harness.addToBattlefield(player1, new BazaarOfWonders());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A spell resolves normally when no card with its name is elsewhere")
    void spellResolvesWhenNameIsUnique() {
        harness.addToBattlefield(player1, new BazaarOfWonders());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // trigger resolves — no match, no counter
        harness.passBothPriorities(); // creature spell resolves

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }
}
