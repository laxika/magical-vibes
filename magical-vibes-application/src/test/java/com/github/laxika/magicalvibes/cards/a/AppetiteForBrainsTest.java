package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BonfireOfTheDamned;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.cards.t.TrustedForcemage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AppetiteForBrains.class, Archangel.class, SeraphOfDawn.class,
        TrustedForcemage.class, Forest.class, BonfireOfTheDamned.class})
class AppetiteForBrainsTest extends BaseCardTest {

    @Test
    @DisplayName("Caster chooses a card with mana value 4 or greater and it is exiled")
    void choosingExpensiveCardExilesIt() {
        harness.setHand(player2, new ArrayList<>(List.of(new Archangel(), new TrustedForcemage())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).choosingPlayerId())
                .isEqualTo(player1.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Archangel"));
        harness.assertNotInGraveyard(player2, "Archangel");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()).get(0).getName()).isEqualTo("Trusted Forcemage");
    }

    @Test
    @DisplayName("Cards with mana value 3 or less are not choosable")
    void cheapCardsExcluded() {
        harness.setHand(player2, new ArrayList<>(List.of(new TrustedForcemage(), new SeraphOfDawn())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Trusted Forcemage has mana value 3; Seraph of Dawn has mana value 4.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);
    }

    @Test
    @DisplayName("Hand without a card of mana value 4 or greater yields no choice")
    void noExpensiveCardNoChoice() {
        harness.setHand(player2, new ArrayList<>(List.of(new TrustedForcemage(), new Forest())));

        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target self; must target an opponent")
    void cannotTargetSelf() {
        harness.setHand(player1, new ArrayList<>(List.of(new AppetiteForBrains(), new SeraphOfDawn())));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Exactly one eligible card must be chosen, even when several are available")
    void mustChooseExactlyOneCard() {
        SeraphOfDawn first = new SeraphOfDawn();
        SeraphOfDawn second = new SeraphOfDawn();
        harness.setHand(player2, List.of(first, second, new TrustedForcemage()));
        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player2, 1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player2.getId())).contains(first).doesNotContain(second).hasSize(2);
        harness.assertNotInGraveyard(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player1, "Appetite for Brains");
    }

    @Test
    @DisplayName("An empty hand resolves without asking for a choice")
    void emptyHandResolves() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Appetite for Brains");
    }

    @Test
    @DisplayName("X in the mana cost of a card in hand contributes zero to its mana value")
    void xCostInHandIsNotEligible() {
        BonfireOfTheDamned bonfire = new BonfireOfTheDamned();
        harness.setHand(player2, List.of(bonfire));
        harness.setHand(player1, List.of(new AppetiteForBrains()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(bonfire);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gameLogContains("reveals their hand:")).isTrue();
        harness.assertInGraveyard(player1, "Appetite for Brains");
    }
}
