package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NoviceOccultist.class, Forest.class, Shock.class})
class NoviceOccultistTest extends BaseCardTest {

    @Test
    void drawsCardAndLosesLifeWhenItDies() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NoviceOccultist());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player1, "Novice Occultist");
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Novice Occultist");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
