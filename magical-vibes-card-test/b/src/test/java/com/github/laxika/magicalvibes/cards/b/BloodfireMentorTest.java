package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodfireMentor.class, GrizzlyBears.class})
class BloodfireMentorTest extends BaseCardTest {

    @Test
    @DisplayName("{2}{U}, {T}: draws a card, then discards a card")
    void lootAbilityDrawsThenDiscards() {
        Permanent mentor = addCreatureReady(player1, new BloodfireMentor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(mentor.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("The drawn card can be chosen for discard and the opponent is unaffected")
    void canDiscardTheDrawnCard() {
        addCreatureReady(player1, new BloodfireMentor());
        BloodfireMentor original = new BloodfireMentor();
        BloodfireMentor drawn = new BloodfireMentor();
        BloodfireMentor opponentsCard = new BloodfireMentor();
        harness.setHand(player1, List.of(original));
        harness.setHand(player2, List.of(opponentsCard));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentsCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("With an empty hand, the card drawn must be discarded")
    void emptyHandStillDrawsThenDiscards() {
        addCreatureReady(player1, new BloodfireMentor());
        BloodfireMentor drawn = new BloodfireMentor();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void cannotActivateWhileSummoningSick() {
        Permanent mentor = addCreatureReady(player1, new BloodfireMentor());
        mentor.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mentor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Mentor cannot pay the tap cost")
    void cannotActivateWhileTapped() {
        Permanent mentor = addCreatureReady(player1, new BloodfireMentor());
        mentor.tap();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the blue mana requirement")
    void cannotActivateWithoutBlueMana() {
        Permanent mentor = addCreatureReady(player1, new BloodfireMentor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(mentor.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
