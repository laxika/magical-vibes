package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiskFactorTest extends BaseCardTest {

    private static final String DAMAGE = "Have Risk Factor deal 4 damage to you";
    private static final String DRAW = "Draw three cards";

    @Test
    @DisplayName("The targeted opponent chooses between damage and drawing")
    void targetedOpponentChoosesMode() {
        castRiskFactor();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.options()).containsExactly(DAMAGE, DRAW);
    }

    @Test
    @DisplayName("Choosing damage deals 4 damage to the targeted opponent")
    void choosingDamageDealsFour() {
        castRiskFactor();

        harness.handleListChoice(player2, DAMAGE);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Choosing cards draws three cards for the spell's controller")
    void choosingCardsDrawsThree() {
        List<Card> cards = List.of(new Forest(), new Plains(), new Forest());
        harness.setLibrary(player1, cards);
        castRiskFactor();

        harness.handleListChoice(player2, DRAW);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .containsExactlyElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Risk Factor cannot target its controller")
    void cannotTargetController() {
        harness.setHand(player1, List.of(new RiskFactor()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Risk Factor requires an opponent target")
    void requiresOpponentTarget() {
        harness.setHand(player1, List.of(new RiskFactor()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jump-start discards a card and exiles Risk Factor after resolution")
    void jumpStartDiscardsAndExiles() {
        RiskFactor spell = new RiskFactor();
        Plains discarded = new Plains();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, DAMAGE);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void castRiskFactor() {
        harness.setHand(player1, List.of(new RiskFactor()));
        addMana();
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
