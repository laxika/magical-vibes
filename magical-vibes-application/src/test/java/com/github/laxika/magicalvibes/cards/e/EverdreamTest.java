package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Everdream.class, Shock.class, GrizzlyBears.class, Regrowth.class})
class EverdreamTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when cast")
    void drawsCardWhenCast() {
        harness.setHand(player1, List.of(new Everdream()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveInstant(player1, 0, List.of());

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Splices onto an instant, draws a card, and stays in hand")
    void splicesOntoInstant() {
        Card shock = new Shock();
        Everdream everdream = new Everdream();
        harness.setHand(player1, List.of(shock, everdream));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithSplice(player1, 0, player2.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        harness.assertInHand(player1, "Everdream");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Splices onto a sorcery and draws only when the host resolves")
    void splicesOntoSorcery() {
        Card returned = new Regrowth();
        Card drawn = new Everdream();
        Card spliced = new Everdream();
        harness.setGraveyard(player1, List.of(returned));
        harness.setHand(player1, List.of(new Regrowth(), spliced));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, returned.getId(), List.of(1));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(spliced, returned, drawn);
        harness.assertInGraveyard(player1, "Regrowth");
    }

    @Test
    @DisplayName("Distinct Everdream copies can both be spliced onto Everdream")
    void splicesMultipleCopies() {
        Card first = new Everdream();
        Card second = new Everdream();
        Card draw1 = new Everdream();
        Card draw2 = new Everdream();
        Card draw3 = new Everdream();
        harness.setHand(player1, List.of(first, new Everdream(), second));
        harness.setLibrary(player1, List.of(draw1, draw2, draw3));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithSplice(player1, 1, null, List.of(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(first, second, draw1, draw2, draw3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Everdream");
    }

    @Test
    @DisplayName("Cannot splice the same card onto a spell twice")
    void rejectsDuplicateSpliceSelection() {
        harness.setHand(player1, List.of(new Everdream(), new Everdream()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1, 1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splicing requires its own blue mana in addition to the host's cost")
    void requiresAdditionalBlueMana() {
        harness.setHand(player1, List.of(new Everdream(), new Everdream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithSplice(player1, 0, null, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not draw if the host's only target becomes illegal")
    void doesNotDrawWhenHostTargetBecomesIllegal() {
        Card target = new Regrowth();
        Card drawn = new Everdream();
        Card spliced = new Everdream();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new Regrowth(), spliced));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castWithSplice(player1, 0, target.getId(), List.of(1));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(spliced);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        harness.assertInGraveyard(player1, "Regrowth");
    }
}
