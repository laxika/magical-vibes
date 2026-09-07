package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouthfulScholar.class, Shock.class})
class YouthfulScholarTest extends BaseCardTest {

    @Test
    @DisplayName("When Youthful Scholar dies, its controller draws two cards")
    void diesDrawsTwoCards() {
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new YouthfulScholar());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player2, 0, scholar.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        harness.assertInGraveyard(player1, "Youthful Scholar");
    }
}
