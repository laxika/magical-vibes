package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArrestersAdmonition;
import com.github.laxika.magicalvibes.cards.m.Mortify;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DebtorsTransport.class, WrathOfGod.class, Mortify.class, ArrestersAdmonition.class})
class DebtorsTransportTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 2 creates two 1/1 white and black Spirit tokens with flying")
    void afterlifeCreatesTwoSpiritTokens() {
        harness.addToBattlefield(player1, new DebtorsTransport());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Debtors' Transport");

        List<Permanent> tokens = findPermanents(player1, "Spirit");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
            assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        });
    }

    @Test
    @DisplayName("Afterlife waits for its trigger to resolve and creates tokens for the dying creature's controller")
    void opponentControlledDeathCreatesTokensForOpponent() {
        Permanent transport = harness.addToBattlefieldAndReturn(player2, new DebtorsTransport());
        harness.setHand(player1, List.of(new Mortify()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, transport.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Debtors' Transport");
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
        assertThat(countPermanents(player1, "Spirit")).isZero();
    }

    @Test
    @DisplayName("Each Transport that dies simultaneously creates its own two Spirits")
    void simultaneousDeathsEachCreateTwoTokens() {
        harness.addToBattlefield(player1, new DebtorsTransport());
        harness.addToBattlefield(player2, new DebtorsTransport());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Debtors' Transport");
        harness.assertInGraveyard(player2, "Debtors' Transport");
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(2);
        assertThat(countPermanents(player2, "Spirit")).isEqualTo(2);
    }

    @Test
    @DisplayName("Returning Transport to hand does not trigger afterlife")
    void returningToHandDoesNotCreateTokens() {
        Permanent transport = harness.addToBattlefieldAndReturn(player2, new DebtorsTransport());
        harness.forceStep(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new ArrestersAdmonition()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, transport.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Debtors' Transport");
        harness.assertNotOnBattlefield(player2, "Debtors' Transport");
        assertThat(countPermanents(player1, "Spirit")).isZero();
        assertThat(countPermanents(player2, "Spirit")).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
