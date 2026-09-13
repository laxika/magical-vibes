package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Persecute.class, AirElemental.class, GrizzlyBears.class, Forest.class})
class PersecuteTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Persecute awaits the caster's color choice")
    void resolvingAwaitsColorChoice() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Target player discards all cards of the chosen color, keeping the rest")
    void discardsAllCardsOfChosenColor() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Air Elemental"));
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("reveals their hand"));
    }

    @Test
    @DisplayName("Choosing a color the target has none of discards nothing")
    void chosenColorAbsentDiscardsNothing() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new AirElemental()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An ordinary colorless land is never discarded")
    void colorlessCardsAreNeverDiscarded() {
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .matches(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Resolving against an empty hand still resolves the color choice with no discards")
    void emptyHandDiscardsNothing() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The target may be the spell's controller")
    void canTargetController() {
        harness.setHand(player1, List.of(new Persecute(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed(DryadArbor.class)
    @DisplayName("A colored land is discarded when it is of the chosen color")
    void discardsColoredLandOfChosenColor() {
        harness.setHand(player2, List.of(new DryadArbor(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Persecute()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player2, "Dryad Arbor");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }
}
