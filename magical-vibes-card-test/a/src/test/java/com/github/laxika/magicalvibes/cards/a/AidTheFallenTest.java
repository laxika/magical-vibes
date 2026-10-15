package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.c.CharmedStray;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({AidTheFallen.class, CharmedStray.class, GideonBlackblade.class})
class AidTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode returns a creature card to hand")
    void creatureModeReturnsCreature() {
        Card creature = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Charmed Stray");
        harness.assertInGraveyard(player1, "Gideon Blackblade");
    }

    @Test
    @DisplayName("Planeswalker mode returns a planeswalker card to hand")
    void planeswalkerModeReturnsPlaneswalker() {
        Card creature = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(planeswalker.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gideon Blackblade");
        harness.assertInGraveyard(player1, "Charmed Stray");
    }

    @Test
    @DisplayName("Both mode returns one creature and one planeswalker")
    void bothModeReturnsBothCards() {
        Card creature = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), planeswalker.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Charmed Stray");
        harness.assertInHand(player1, "Gideon Blackblade");
    }

    @Test
    @DisplayName("Both mode excludes cards that are neither creatures nor planeswalkers")
    void bothModeExcludesOtherCards() {
        Card creature = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        Card spell = new AidTheFallen();
        harness.setGraveyard(player1, List.of(creature, planeswalker, spell));
        castAidTheFallen(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), planeswalker.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Charmed Stray");
        harness.assertInHand(player1, "Gideon Blackblade");
        harness.assertInGraveyard(player1, "Aid the Fallen");
    }

    @Test
    void bothModeRejectsTwoCreatures() {
        Card first = new CharmedStray();
        Card second = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(first, second, planeswalker));
        castAidTheFallen(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeRejectsTwoPlaneswalkers() {
        Card creature = new CharmedStray();
        Card first = new GideonBlackblade();
        Card second = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, first, second));
        castAidTheFallen(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureModeRequiresATarget() {
        harness.setGraveyard(player1, List.of(new CharmedStray()));
        castAidTheFallen(0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void planeswalkerModeRequiresATarget() {
        harness.setGraveyard(player1, List.of(new GideonBlackblade()));
        castAidTheFallen(1);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeRequiresBothTargets() {
        Card creature = new CharmedStray();
        harness.setGraveyard(player1, List.of(creature, new GideonBlackblade()));
        castAidTheFallen(2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void creatureModeExcludesOpponentsGraveyard() {
        Card ownCreature = new CharmedStray();
        Card opponentsCreature = new CharmedStray();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        castAidTheFallen(0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(opponentsCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Charmed Stray");
        harness.assertInGraveyard(player2, "Charmed Stray");
    }

    @Test
    void bothModeReturnsRemainingLegalTarget() {
        Card creature = new CharmedStray();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), planeswalker.getId()));
        harness.setGraveyard(player1, List.of(planeswalker));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gideon Blackblade");
        harness.assertNotInHand(player1, "Charmed Stray");
        harness.assertInGraveyard(player1, "Aid the Fallen");
    }

    private void castAidTheFallen(int mode) {
        harness.setHand(player1, List.of(new AidTheFallen()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, mode);
    }
}
