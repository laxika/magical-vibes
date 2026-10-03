package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NomadicElf;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.w.WorldlyCounsel;
import com.github.laxika.magicalvibes.cards.u.Undermine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Addle.class, WorldlyCounsel.class, Opt.class, NomadicElf.class, Forest.class, Undermine.class})
class AddleTest extends BaseCardTest {

    @Test
    void emptyHandStillAllowsColorChoiceAndFinishesWithoutDiscard() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Addle");
    }

    @ParameterizedTest
    @ValueSource(strings = {"BLUE", "BLACK"})
    void multicoloredCardCanBeDiscardedForEitherOfItsColors(String color) {
        harness.setHand(player2, List.of(new Undermine(), new Forest()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, color);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(0);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Undermine");
        harness.assertInHand(player2, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void casterMustChooseMatchingCardAndTargetCannotChooseInstead() {
        harness.setHand(player2, List.of(new Opt(), new NomadicElf()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThatThrownBy(() -> harness.handleCardChosen(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Opt");
        harness.assertInHand(player2, "Nomadic Elf");
    }

    @Test
    @DisplayName("Resolving Addle first prompts for a color")
    void resolvingPromptsForColor() {
        harness.setHand(player2, List.of(new WorldlyCounsel()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("The chosen color limits the card choice and discards exactly one matching card")
    void choosesOneCardOfChosenColor() {
        harness.setHand(player2, List.of(new WorldlyCounsel(), new Opt(), new NomadicElf(), new Forest()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "BLUE");

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Worldly Counsel");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Opt", "Nomadic Elf", "Forest");
    }

    @Test
    @DisplayName("The target hand is publicly revealed before the matching card is chosen")
    void revealsTargetHandBeforeChoosingCard() {
        harness.setHand(player2, List.of(new WorldlyCounsel(), new Forest()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "BLUE");

        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(gameLogContains("Worldly Counsel")).isTrue();

        harness.handleCardChosen(player1, 0);
    }

    @Test
    @DisplayName("A colorless basic land is not treated as a card of the color it produces")
    void colorlessLandIsNotAColorMatch() {
        harness.setHand(player2, List.of(new Forest(), new NomadicElf()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "GREEN");

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Nomadic Elf");
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("The caster may target their own hand")
    void canTargetOwnHand() {
        harness.setHand(player1, List.of(new Addle(), new WorldlyCounsel()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player1.getId());
        harness.handleListChoice(player1, "BLUE");
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Worldly Counsel");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A color with no matching card leaves the target hand unchanged")
    void noMatchingColorDoesNothing() {
        harness.setHand(player2, List.of(new WorldlyCounsel(), new NomadicElf(), new Forest()));
        harness.setHand(player1, List.of(new Addle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).extracting(c -> c.getName())
                .containsExactly("Worldly Counsel", "Nomadic Elf", "Forest");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }
}
