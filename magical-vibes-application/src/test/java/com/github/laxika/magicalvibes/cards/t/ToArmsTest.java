package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToArms.class, GrizzlyBears.class, Forest.class})
class ToArmsTest extends BaseCardTest {

    @Test
    void untapsOwnCreaturesAndDrawsACard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        ownLand.tap();
        opponentCreature.tap();

        GrizzlyBears drawCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawCard));
        harness.setHand(player1, List.of(new ToArms()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawCard);
    }
}
