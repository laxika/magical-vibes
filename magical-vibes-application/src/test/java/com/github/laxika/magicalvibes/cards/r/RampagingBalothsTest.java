package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RampagingBaloths.class, Forest.class})
class RampagingBalothsTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 4/4 green Beast token")
    void landfallCreatesBeast() {
        harness.addToBattlefield(player1, new RampagingBaloths());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getEffectivePower()).isEqualTo(4);
        assertThat(beast.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("An opponent's landfall does not create a Beast token")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new RampagingBaloths());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isZero();
    }

    @Test
    @DisplayName("Lands put onto the battlefield each create a token in the same turn")
    void multipleLandEntriesEachTrigger() {
        harness.addToBattlefield(player1, new RampagingBaloths());

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);

        harness.enterBattlefieldAndReturn(player1, new Forest());
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
        assertThat(countPermanents(player2, "Beast")).isZero();
    }

    @Test
    @DisplayName("Each Baloths triggers independently for a land")
    void multipleBalothsEachTrigger() {
        harness.addToBattlefield(player1, new RampagingBaloths());
        harness.addToBattlefield(player1, new RampagingBaloths());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(2);
    }

    @Test
    @DisplayName("The created Beast is a green creature token without trample")
    void tokenHasCorrectCharacteristics() {
        harness.addToBattlefield(player1, new RampagingBaloths());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        Permanent beast = findPermanent(player1, "Beast");
        assertThat(beast.getCard().isToken()).isTrue();
        assertThat(beast.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(beast.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(beast.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(beast.getCard().getKeywords()).isEmpty();
        assertThat(beast.isTapped()).isFalse();
        assertThat(beast.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("A creature entering does not trigger landfall")
    void nonlandDoesNotTrigger() {
        harness.addToBattlefield(player1, new RampagingBaloths());

        harness.enterBattlefieldAndReturn(player1, new RampagingBaloths());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Beast")).isZero();
    }
}
