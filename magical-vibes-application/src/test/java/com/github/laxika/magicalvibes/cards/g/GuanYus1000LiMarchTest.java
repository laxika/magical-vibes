package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

class GuanYus1000LiMarchTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys tapped creatures on both sides")
    void destroysTappedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new LlanowarElves());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        bears.tap();
        Permanent elves = findPermanent(player2, "Llanowar Elves");
        elves.tap();

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Untapped creatures are not destroyed")
    void untappedCreaturesSurvive() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Indestructible tapped creature survives")
    void indestructibleTappedCreatureSurvives() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.tap();
        bears.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);

        harness.setHand(player1, List.of(new GuanYus1000LiMarch()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
