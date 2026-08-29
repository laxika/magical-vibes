package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThranLensTest extends BaseCardTest {

    @Test
    @DisplayName("Makes every battlefield permanent colorless")
    void makesEveryBattlefieldPermanentColorless() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent lens = harness.addToBattlefieldAndReturn(player1, new ThranLens());

        assertThat(gqs.getEffectiveColors(gd, ownCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, opponentCreature)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
        assertThat(gqs.getEffectiveColors(gd, lens)).isEmpty();
    }

    @Test
    @DisplayName("Does not make cards outside the battlefield colorless")
    void leavesCardsOutsideBattlefieldUnchanged() {
        harness.addToBattlefield(player1, new ThranLens());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectiveCardColors(gd, gd.playerHands.get(player1.getId()).getFirst()))
                .containsExactly(CardColor.GREEN);
    }
}
