package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BonebindOrator;
import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicWhorl.class, BonebindOrator.class, Island.class, DaggerfangDuo.class})
class PsychicWhorlTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent discards two cards")
    void targetOpponentDiscardsTwoCards() {
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of(new BonebindOrator(), new Island(), new BonebindOrator()));
        harness.setLibrary(player1, List.of(new Island(), new BonebindOrator()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Controlling a Rat surveils two after the discard")
    void controllingRatSurveilsTwoAfterDiscard() {
        Card topCard = new BonebindOrator();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.addToBattlefield(player1, new DaggerfangDuo());
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of(new BonebindOrator(), new Island()));
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard, secondCard);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetYourself() {
        harness.setHand(player1, List.of(new PsychicWhorl()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    @Test
    void opponentWithOneCardDiscardsItAndCasterStillSurveils() {
        Card discarded = new Island();
        Card top = new BonebindOrator();
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new DaggerfangDuo());
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyOpponentHandStillAllowsSurveilAndSplitBetweenLibraryAndGraveyard() {
        Card top = new BonebindOrator();
        Card second = new Island();
        Card third = new Island();
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top, second, third));
        harness.addToBattlefield(player1, new DaggerfangDuo());
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentsRatDoesNotAllowCasterToSurveil() {
        Card top = new Island();
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player2, new DaggerfangDuo());
        addMana();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    void ratAcquiredAfterCastingAllowsSurveil() {
        Card top = new Island();
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top));
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.addToBattlefield(player1, new DaggerfangDuo());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(top);
    }

    @Test
    void ratLostBeforeResolutionDoesNotAllowSurveil() {
        Card top = new Island();
        harness.setHand(player1, List.of(new PsychicWhorl()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new DaggerfangDuo());
        addMana();

        harness.castSorcery(player1, 0, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }
}
