package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({EnatuGolem.class, WrathOfGod.class, Regress.class})
class EnatuGolemTest extends BaseCardTest {

    @Test
    @DisplayName("When Enatu Golem dies, its controller gains 4 life")
    void gainsLifeWhenItDies() {
        harness.addToBattlefield(player1, new EnatuGolem());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Each Golem dying simultaneously gives its own controller 4 life")
    void simultaneousDeathsGainLifeForEachController() {
        harness.addToBattlefield(player1, new EnatuGolem());
        harness.addToBattlefield(player1, new EnatuGolem());
        harness.addToBattlefield(player2, new EnatuGolem());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 14);
        harness.assertNotOnBattlefield(player1, "Enatu Golem");
        harness.assertNotOnBattlefield(player2, "Enatu Golem");
    }

    @Test
    @DisplayName("The controller at death gains life even when another player owns the Golem")
    void controllerRatherThanOwnerGainsLife() {
        EnatuGolem golem = new EnatuGolem();
        golem.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, golem);
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castAndResolveSorcery(player1, 0, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 14);
        harness.assertInGraveyard(player1, "Enatu Golem");
        harness.assertNotInGraveyard(player2, "Enatu Golem");
    }

    @Test
    @DisplayName("Returning Enatu Golem to hand does not trigger life gain")
    void returningToHandDoesNotGainLife() {
        harness.addToBattlefield(player1, new EnatuGolem());
        harness.setHand(player1, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 10);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Enatu Golem"));
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertInHand(player1, "Enatu Golem");
        harness.assertNotOnBattlefield(player1, "Enatu Golem");
        harness.assertNotInGraveyard(player1, "Enatu Golem");
    }
}
