package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Tonberry.class)
class TonberryTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with a stun counter")
    void entersTappedWithStunCounter() {
        Permanent tonberry = castTonberry();

        assertThat(tonberry.isTapped()).isTrue();
        assertThat(tonberry.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Has first strike and deathtouch during its controller's turn")
    void hasKeywordsDuringControllerTurn() {
        Permanent tonberry = castTonberry();

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.DEATHTOUCH)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tonberry, Keyword.DEATHTOUCH)).isFalse();
    }

    private Permanent castTonberry() {
        harness.castFromHand(player1, new Tonberry(), "{B}");
        harness.passBothPriorities();
        return findPermanent(player1, "Tonberry");
    }

    @Test
    @DisplayName("Stun counter replaces the first untap, but not the next")
    void stunCounterDelaysUntappingForOneUntapStep() {
        Permanent tonberry = castTonberry();

        harness.performUntapStep(player2);
        assertThat(tonberry.isTapped()).isTrue();
        assertThat(tonberry.getCounterCount(CounterType.STUN)).isEqualTo(1);

        harness.performUntapStep(player1);
        assertThat(tonberry.isTapped()).isTrue();
        assertThat(tonberry.getCounterCount(CounterType.STUN)).isZero();

        harness.performUntapStep(player2);
        harness.performUntapStep(player1);
        assertThat(tonberry.isTapped()).isFalse();
        assertThat(tonberry.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    @DisplayName("Chef's Knife follows each Tonberry's controller and only affects itself")
    void keywordsAreLimitedToTheActiveControllersTonberry() {
        Permanent ownTonberry = castTonberry();
        Permanent opposingTonberry = addCreatureReady(player2, new Tonberry());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, ownTonberry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownTonberry, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingTonberry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingTonberry, Keyword.DEATHTOUCH)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, ownTonberry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, ownTonberry, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingTonberry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingTonberry, Keyword.DEATHTOUCH)).isTrue();
    }
}
