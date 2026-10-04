package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Godsire.class})
class GodsireTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping creates an 8/8 Beast token")
    void createsBeastToken() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        Permanent token = findPermanent(player1, "Beast");
        assertThat(token.getEffectivePower()).isEqualTo(8);
        assertThat(token.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("Created Beast is tricolored, untapped, and does not inherit vigilance")
    void tokenHasSpecifiedCharacteristics() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Beast");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(
                CardColor.RED, CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.BEAST);
        assertThat(token.getCard().getKeywords()).doesNotContain(Keyword.VIGILANCE);
        assertThat(token.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tap cost is paid before the token ability resolves")
    void paysTapCostBeforeResolution() {
        Permanent godsire = addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(godsire.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Beast")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the tap ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new Godsire());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Godsire").isTapped()).isFalse();
        assertThat(countPermanents(player1, "Beast")).isZero();
    }

    @Test
    @DisplayName("Ability can create a token during the opponent's turn")
    void canActivateOnOpponentsTurn() {
        addCreatureReady(player1, new Godsire());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Beast")).isEqualTo(1);
        assertThat(countPermanents(player2, "Beast")).isZero();
    }
}
