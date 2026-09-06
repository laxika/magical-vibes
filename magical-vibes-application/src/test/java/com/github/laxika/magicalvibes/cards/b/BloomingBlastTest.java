package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloomingBlast.class, GrizzlyBears.class, Plains.class})
class BloomingBlastTest extends BaseCardTest {

    @Test
    void withoutGiftDealsTwoDamageAndNoControllerDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloomingBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithGift(player1, 0, bear.getId(), false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Treasure");
    }

    @Test
    void withGiftCreatesTreasureAndDamagesTargetControllers() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BloomingBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithGift(player1, 0, bear.getId(), true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player2, "Treasure");
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BloomingBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, plains.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }
}
