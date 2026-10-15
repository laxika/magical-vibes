package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SinisterSabotage;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiskFactor.class, Forest.class, Plains.class, SinisterSabotage.class})
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

    @Test
    @DisplayName("Declining damage after jump-start draws three cards and exiles the spell")
    void jumpStartDrawsAndExiles() {
        RiskFactor spell = new RiskFactor();
        RiskFactor discarded = new RiskFactor();
        List<Card> cards = List.of(new RiskFactor(), new RiskFactor(), new RiskFactor());
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, cards);
        addMana();

        harness.castJumpStart(player1, 0, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards);

        harness.passBothPriorities();
        harness.handleListChoice(player2, DRAW);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Jump-start requires a card to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new RiskFactor()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jump-start requires the full mana cost before discarding")
    void jumpStartRequiresMana() {
        RiskFactor spell = new RiskFactor();
        RiskFactor discarded = new RiskFactor();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Countering a jump-started Risk Factor exiles it without damage or draws")
    void counteredJumpStartIsExiled() {
        RiskFactor spell = new RiskFactor();
        RiskFactor discarded = new RiskFactor();
        RiskFactor topCard = new RiskFactor();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(new RiskFactor()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castJumpStart(player1, 0, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    private void castRiskFactor() {
        harness.setHand(player1, List.of(new RiskFactor()));
        addMana();
        harness.castAndResolveInstant(player1, 0, player2.getId());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
