package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ErtaisTrickery.class, ArcticMerfolk.class})
class ErtaisTrickeryTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a kicked spell")
    void countersKickedSpell() {
        Permanent returnedForKicker = harness.addToBattlefieldAndReturn(player1, new ArcticMerfolk());
        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.setHand(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castKickedCreatureWithPermanent(player1, 0, returnedForKicker.getId());
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, merfolk.getId());

        harness.assertInGraveyard(player1, "Arctic Merfolk");
        harness.assertInHand(player1, "Arctic Merfolk");
    }

    @Test
    @DisplayName("Can target an unkicked spell but does not counter it")
    void canTargetNonKickedSpellWithoutCounteringIt() {
        ArcticMerfolk merfolk = new ArcticMerfolk();
        harness.setHand(player1, List.of(merfolk));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        harness.setHand(player2, List.of(new ErtaisTrickery()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, merfolk.getId());
        harness.assertInGraveyard(player2, "Ertai's Trickery");
        harness.assertNotInGraveyard(player1, "Arctic Merfolk");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Arctic Merfolk");
        harness.assertNotInGraveyard(player1, "Arctic Merfolk");
    }
}
