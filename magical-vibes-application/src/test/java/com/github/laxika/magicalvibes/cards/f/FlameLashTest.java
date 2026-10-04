package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({FlameLash.class, GrizzlyBears.class})
class FlameLashTest extends BaseCardTest {

    @Test
    void dealsFourDamageToTargetCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFlameLash(creature.getId());

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void dealsFourDamageToTargetPlayer() {
        castFlameLash(player2.getId());

        harness.assertLife(player2, 16);
    }

    private void castFlameLash(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new FlameLash()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
