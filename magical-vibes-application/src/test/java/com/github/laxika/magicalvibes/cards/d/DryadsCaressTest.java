package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DryadsCaress.class, GrizzlyBears.class})
class DryadsCaressTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 1 life for each creature on the battlefield")
    void gainsLifeForEachCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownOtherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        ownOtherCreature.tap();
        opposingCreature.tap();

        harness.setLife(player1, 20);
        castWithGreenOnly();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(ownCreature.isTapped()).isTrue();
        assertThat(ownOtherCreature.isTapped()).isTrue();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untaps your creatures when white mana was spent")
    void untapsOwnCreaturesWhenWhiteWasSpent() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ownCreature.tap();
        opposingCreature.tap();

        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new DryadsCaress()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(opposingCreature.isTapped()).isTrue();
    }

    private void castWithGreenOnly() {
        harness.setHand(player1, List.of(new DryadsCaress()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
