package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReadTheRunes.class, Forest.class, ElvishWarrior.class})
class ReadTheRunesTest extends BaseCardTest {

    @Test
    @DisplayName("Repeats the sacrifice-or-discard choice once for each card drawn")
    void repeatsChoiceForEachCardDrawn() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        harness.assertOnBattlefield(player1, "Elvish Warrior");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.SACRIFICE);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Repeats only for cards actually drawn when the library is short")
    void repeatsOnlyForCardsActuallyDrawn() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Applies the repeated choice only to the spell's controller")
    void appliesChoiceOnlyToController() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.setHand(player2, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, 1, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());

        harness.handleListChoice(player1, ChoiceContext.EachPlayerSacrificeOrDiscardChoice.DISCARD);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Elvish Warrior");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("X=0 draws no cards and creates no sacrifice-or-discard choice")
    void drawsNothingAndCreatesNoChoiceWhenXIsZero() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.setHand(player1, List.of(new ReadTheRunes()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player1, "Elvish Warrior");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
