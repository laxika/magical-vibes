package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AviationPioneer.class, Murder.class})
class AviationPioneerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates a 1/1 colorless Thopter artifact creature token with flying")
    void etbCreatesThopterToken() {
        harness.castFromHand(player1, new AviationPioneer(), "{2}{U}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

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
    @DisplayName("The token is created only when the enter trigger resolves")
    void tokenCreationUsesTheStack() {
        harness.castFromHand(player1, new AviationPioneer(), "{2}{U}");
        assertThat(countPermanents(player1, "Thopter")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Aviation Pioneer")).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        Permanent thopter = findPermanent(player1, "Thopter");
        assertThat(thopter.getCard().isToken()).isTrue();
        assertThat(thopter.getCard().getSubtypes()).contains(CardSubtype.THOPTER);
        assertThat(thopter.isTapped()).isFalse();
        assertThat(thopter.isSummoningSick()).isTrue();
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    @DisplayName("Removing the Pioneer in response does not stop its enter trigger")
    void triggerResolvesAfterSourceIsDestroyed() {
        harness.castFromHand(player1, new AviationPioneer(), "{2}{U}");
        harness.passBothPriorities();
        Permanent pioneer = findPermanent(player1, "Aviation Pioneer");

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, pioneer.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Aviation Pioneer")).isZero();
        assertThat(countPermanents(player1, "Thopter")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player2, "Thopter")).isZero();
    }

    @Test
    @DisplayName("Entering without being cast creates a token for the entering creature's controller")
    void enteringUnderOpponentControlCreatesTheirToken() {
        harness.enterBattlefieldAndReturn(player2, new AviationPioneer());
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Thopter")).isEqualTo(1);
        assertThat(countPermanents(player1, "Thopter")).isZero();
    }
}
