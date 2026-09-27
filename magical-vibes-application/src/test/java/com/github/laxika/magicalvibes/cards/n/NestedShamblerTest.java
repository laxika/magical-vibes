package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NestedShambler.class, GiantGrowth.class, WrathOfGod.class})
class NestedShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("When Nested Shambler dies, it creates tapped green Squirrels equal to its power")
    void deathCreatesTappedSquirrelsEqualToPower() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new NestedShambler());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, shambler.getId());

        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> squirrels = findPermanents(player1, "Squirrel");
        assertThat(squirrels).hasSize(4);
        assertThat(squirrels).allSatisfy(squirrel -> {
            assertThat(squirrel.isTapped()).isTrue();
            assertThat(squirrel.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(squirrel.getCard().getSubtypes()).contains(CardSubtype.SQUIRREL);
            assertThat(squirrel.getEffectivePower()).isEqualTo(1);
            assertThat(squirrel.getEffectiveToughness()).isEqualTo(1);
        });
    }
}
