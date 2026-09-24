package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HandOfHonor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({SpiralingEmbers.class, HandOfHonor.class})
class SpiralingEmbersTest extends BaseCardTest {

    @Test
    void dealsDamageToPlayerEqualToControllerHandSize() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(
                new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers(), new SpiralingEmbers()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 17);
    }

    @Test
    void usesControllerHandSizeOnResolutionAndCanTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HandOfHonor());
        harness.setHand(player1, List.of(new SpiralingEmbers(), new SpiralingEmbers()));
        addMana(player1);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerHands.get(player1.getId()).add(new SpiralingEmbers());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Hand of Honor");
    }

    @Test
    void dealsNoDamageWithNoCardsRemainingInHand() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new SpiralingEmbers()));
        addMana(player1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
    }

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.RED, 4);
    }
}
