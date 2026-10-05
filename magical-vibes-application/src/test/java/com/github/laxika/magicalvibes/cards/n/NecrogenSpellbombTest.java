package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(NecrogenSpellbomb.class)
class NecrogenSpellbombTest extends BaseCardTest {

    @Test
    @DisplayName("The black ability makes the target player discard a card")
    void targetPlayerDiscardsACard() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        harness.setHand(player2, List.of(new NecrogenSpellbomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Necrogen Spellbomb");
        harness.assertInGraveyard(player1, "Necrogen Spellbomb");
    }

    @Test
    @DisplayName("The black ability can target its controller")
    void canTargetController() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        harness.setHand(player1, List.of(new NecrogenSpellbomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The black ability resolves without a discard when the target has no cards")
    void resolvesWhenTargetHasNoCards() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Necrogen Spellbomb");
    }

    @Test
    @DisplayName("The colorless ability draws a card and sacrifices the artifact")
    void drawsACard() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        harness.setLibrary(player1, List.of(new NecrogenSpellbomb()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        harness.assertInHand(player1, "Necrogen Spellbomb");
        harness.assertInGraveyard(player1, "Necrogen Spellbomb");
    }

    @Test
    @DisplayName("The discard ability cannot target a permanent")
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        harness.addToBattlefield(player2, new NecrogenSpellbomb());
        harness.addMana(player1, ManaColor.BLACK, 1);

        var spellbombId = findPermanent(player2, "Necrogen Spellbomb").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, spellbombId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a player");

        harness.assertOnBattlefield(player1, "Necrogen Spellbomb");
    }
    @Test
    @DisplayName("Sacrifice is paid before the target player chooses exactly one card to discard")
    void sacrificesAtActivationAndTargetChoosesOneCard() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        var retainedCard = new NecrogenSpellbomb();
        var discardedCard = new NecrogenSpellbomb();
        harness.setHand(player2, List.of(retainedCard, discardedCard));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        harness.assertNotOnBattlefield(player1, "Necrogen Spellbomb");
        harness.assertInGraveyard(player1, "Necrogen Spellbomb");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard, discardedCard);

        harness.passBothPriorities();
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A tapped Spellbomb can be sacrificed to draw using colored mana for the generic cost")
    void tappedSpellbombDrawsUsingColoredMana() {
        harness.addToBattlefield(player1, new NecrogenSpellbomb());
        findPermanent(player1, "Necrogen Spellbomb").tap();
        harness.setHand(player1, List.of());
        var drawnCard = new NecrogenSpellbomb();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Necrogen Spellbomb");
        harness.assertInGraveyard(player1, "Necrogen Spellbomb");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }
}
