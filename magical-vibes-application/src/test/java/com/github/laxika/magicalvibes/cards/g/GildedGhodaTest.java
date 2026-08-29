package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GildedGhodaTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled creates a Treasure token")
    void attacksWhileSaddledCreatesTreasure() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());
        ghoda.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not create a Treasure token")
    void doesNotCreateTreasureWhenNotSaddled() {
        addCreatureReady(player1, new GildedGhoda());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent ghoda = addCreatureReady(player1, new GildedGhoda());

        declareAttackers(player1, List.of(0));
        ghoda.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
