package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LocustSpray;
import com.github.laxika.magicalvibes.cards.l.LoxodonSurveyor;
import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
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

@CardUsed({IntimidationTactics.class, Forest.class, LocustSpray.class,
        LoxodonSurveyor.class, LumberingWorldwagon.class})
class IntimidationTacticsTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals an opponent's hand and allows choosing an artifact or creature to exile")
    void choosesArtifactOrCreatureToExile() {
        Card creature = new LoxodonSurveyor();
        Card artifact = new LumberingWorldwagon();
        Card instant = new LocustSpray();
        harness.setHand(player2, List.of(creature, artifact, instant));

        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0, 1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactly(artifact);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Loxodon Surveyor", "Locust Spray");
    }

    @Test
    @DisplayName("Does nothing when the opponent has no artifact or creature card")
    void noValidCardDoesNothing() {
        harness.setHand(player2, List.of(new Forest(), new LocustSpray()));
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target the caster")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Intimidation Tactics and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.setLibrary(player1, List.of(new LoxodonSurveyor()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Intimidation Tactics");
        harness.assertInHand(player1, "Loxodon Surveyor");
    }

    @Test
    @DisplayName("Exiles exactly the chosen creature, leaving another copy in hand")
    void exilesOnlyChosenCreature() {
        Card chosen = new LoxodonSurveyor();
        Card otherCopy = new LoxodonSurveyor();
        Card artifact = new LumberingWorldwagon();
        harness.setHand(player2, List.of(chosen, otherCopy, artifact));
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(chosen);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(otherCopy, artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Intimidation Tactics");
    }

    @Test
    @DisplayName("Resolves against an empty hand without requesting a choice")
    void emptyHandDoesNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Intimidation Tactics");
    }

    @Test
    @DisplayName("The caster must choose a legal card and the opponent cannot make the choice")
    void rejectsIllegalChoicesAndDeclining() {
        Card creature = new LoxodonSurveyor();
        Card instant = new LocustSpray();
        Card land = new Forest();
        harness.setHand(player2, List.of(creature, instant, land));
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(creature, instant, land);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(instant, land);
    }

    @Test
    @DisplayName("Cycling pays three generic mana and discards before the draw resolves")
    void cyclingPaysCostsBeforeDrawing() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Intimidation Tactics");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with less than three mana")
    void cyclingRequiresThreeMana() {
        harness.setHand(player1, List.of(new IntimidationTactics()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Intimidation Tactics");
        harness.assertNotInGraveyard(player1, "Intimidation Tactics");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
