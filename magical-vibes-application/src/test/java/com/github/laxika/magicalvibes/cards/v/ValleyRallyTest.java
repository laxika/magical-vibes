package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValleyRally.class, GrizzlyBears.class})
class ValleyRallyTest extends BaseCardTest {

    @Test
    void withoutGiftBoostsAllYourCreaturesAndNeedsNoTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(null, false);

        assertThat(firstBear.getPowerModifier()).isEqualTo(2);
        assertThat(secondBear.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, firstBear, Keyword.FIRST_STRIKE)).isFalse();
        harness.assertNotOnBattlefield(player2, "Food");
    }

    @Test
    void promisingGiftCreatesFoodBoostsYourCreaturesAndGrantsFirstStrikeToTheTarget() {
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent targetBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(targetBear, true);

        assertThat(firstBear.getPowerModifier()).isEqualTo(2);
        assertThat(targetBear.getPowerModifier()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, targetBear, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertOnBattlefield(player2, "Food");
    }

    @Test
    void promisedGiftRequiresATargetCreatureYouControl() {
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castInstantWithGift(player1, 0, opponentBear.getId(), true))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    private void cast(Permanent target, boolean giftPromised) {
        prepareSpell();
        harness.castInstantWithGift(player1, 0, target == null ? null : target.getId(), giftPromised);
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new ValleyRally()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
