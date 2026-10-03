package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcolyteOfAffliction.class, Forest.class, GrizzlyBears.class, Shock.class})
class AcolyteOfAfflictionTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills two cards, then may return any permanent from the graveyard")
    void millsThenMayReturnPermanent() {
        GrizzlyBears permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent, new Shock()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock()));

        castAndResolveToMay();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid card index");
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the return leaves the milled cards in the graveyard")
    void decliningReturnLeavesMilledCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Forest");
        harness.assertOnBattlefield(player1, "Acolyte of Affliction");
    }

    @Test
    @DisplayName("A land milled by the ability can be returned, and only two cards are milled")
    void returnsNewlyMilledLand() {
        Forest land = new Forest();
        Shock milledSpell = new Shock();
        Forest remainingCard = new Forest();
        harness.setLibrary(player1, List.of(land, milledSpell, remainingCard));
        harness.setLibrary(player2, List.of(new Forest()));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(milledSpell);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library does not prevent returning an existing permanent")
    void returnsPermanentWithEmptyLibrary() {
        Forest land = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(land));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With no eligible permanent, accepting the return finishes without using an opponent's graveyard")
    void noEligiblePermanentFinishesResolution() {
        Shock spell = new Shock();
        Forest opponentsLand = new Forest();
        harness.setLibrary(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(opponentsLand));

        castAndResolveToMay();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentsLand);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castAndResolveToMay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AcolyteOfAffliction()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
