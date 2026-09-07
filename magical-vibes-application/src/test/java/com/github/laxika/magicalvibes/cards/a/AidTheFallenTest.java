package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({AidTheFallen.class, GrizzlyBears.class, GideonBlackblade.class})
class AidTheFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode returns a creature card to hand")
    void creatureModeReturnsCreature() {
        Card creature = new GrizzlyBears();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Gideon Blackblade");
    }

    @Test
    @DisplayName("Planeswalker mode returns a planeswalker card to hand")
    void planeswalkerModeReturnsPlaneswalker() {
        Card creature = new GrizzlyBears();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(planeswalker.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Gideon Blackblade");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both mode returns one creature and one planeswalker")
    void bothModeReturnsBothCards() {
        Card creature = new GrizzlyBears();
        Card planeswalker = new GideonBlackblade();
        harness.setGraveyard(player1, List.of(creature, planeswalker));
        castAidTheFallen(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId(), planeswalker.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Gideon Blackblade");
    }

    @Test
    @DisplayName("Both mode excludes cards that are neither creatures nor planeswalkers")
    void bothModeExcludesOtherCards() {
        Card creature = new GrizzlyBears();
        Card spell = new AidTheFallen();
        harness.setGraveyard(player1, List.of(creature, spell));
        castAidTheFallen(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    private void castAidTheFallen(int mode) {
        harness.setHand(player1, List.of(new AidTheFallen()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, mode);
    }
}
