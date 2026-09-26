package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.t.TangleSpider;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PulseOfTheTangle.class, TangleSpider.class, DarksteelCitadel.class})
class PulseOfTheTangleTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 3/3 Beast and returns to hand when an opponent still has more creatures")
    void createsBeastAndReturnsToHand() {
        harness.addToBattlefield(player2, new TangleSpider());
        harness.addToBattlefield(player2, new TangleSpider());
        PulseOfTheTangle pulse = cast();

        Permanent beast = findBeastToken();
        assertThat(beast.getEffectivePower()).isEqualTo(3);
        assertThat(beast.getEffectiveToughness()).isEqualTo(3);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(gd.playerHands.get(player1.getId())).contains(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    @Test
    @DisplayName("Checks the creature count after creating the token")
    void doesNotReturnWhenTheTokenMakesTheCountsEqual() {
        harness.addToBattlefield(player2, new TangleSpider());
        PulseOfTheTangle pulse = cast();

        assertThat(findBeastToken()).isNotNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pulse);
    }

    @Test
    @DisplayName("Counts creatures rather than all permanents for the return condition")
    void returnsWhenOpponentHasMoreCreaturesThanYouDespiteYourExtraNoncreaturePermanent() {
        harness.addToBattlefield(player1, new DarksteelCitadel());
        harness.addToBattlefield(player2, new TangleSpider());
        harness.addToBattlefield(player2, new TangleSpider());
        PulseOfTheTangle pulse = cast();

        assertThat(gd.playerHands.get(player1.getId())).contains(pulse);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pulse);
    }

    private PulseOfTheTangle cast() {
        PulseOfTheTangle pulse = new PulseOfTheTangle();
        harness.castFromHand(player1, pulse, "{1}{G}{G}");
        harness.passBothPriorities();
        return pulse;
    }

    private Permanent findBeastToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BEAST))
                .findFirst()
                .orElseThrow();
    }
}
