package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NimbleThopterist.class})
class NimbleThopteristTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.castFromHand(player1, new NimbleThopterist(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thopter = findPermanent(player1, "Thopter");

        assertThat(thopter).isNotNull();
        assertThat(thopter.getCard().getName()).isEqualTo("Thopter");
        assertThat(thopter.getCard().getPower()).isEqualTo(1);
        assertThat(thopter.getCard().getToughness()).isEqualTo(1);
        assertThat(thopter.getCard().getColors()).isEmpty();
        assertThat(thopter.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(thopter.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(thopter.getCard().hasType(CardType.CREATURE)).isTrue();
    }

    @Test
    @DisplayName("Entering without being cast creates exactly one untapped Thopter for the controller")
    void enteringWithoutCastingCreatesToken() {
        harness.enterBattlefieldAndReturn(player2, new NimbleThopterist());

        resolveAllTriggers();

        assertThat(findPermanents(player2, "Thopter")).hasSize(1);
        Permanent thopter = findPermanent(player2, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getSubtypes()).containsExactly(CardSubtype.THOPTER);
        assertThat(thopter.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Thopter");
    }

    @Test
    @DisplayName("The enter trigger creates its token even if Nimble Thopterist dies first")
    void triggerResolvesAfterSourceDies() {
        harness.castFromHand(player1, new NimbleThopterist(), "{3}{U}");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Thopter");

        Permanent source = findPermanent(player1, "Nimble Thopterist");
        source.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.assertNotOnBattlefield(player1, "Nimble Thopterist");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Thopter")).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Thopter");
    }
}
