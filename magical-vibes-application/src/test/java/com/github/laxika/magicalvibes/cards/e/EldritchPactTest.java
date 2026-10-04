package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.ChainsOfMephistopheles;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EldritchPact.class, Forest.class, GrizzlyBears.class, ChainsOfMephistopheles.class})
class EldritchPactTest extends BaseCardTest {

    @Test
    @DisplayName("Target player draws and loses life equal to cards in their graveyard")
    void targetPlayerDrawsAndLosesLifeForTheirGraveyard() {
        List<Card> drawnCards = List.of(new Forest(), new Forest(), new Forest());
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, drawnCards);
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Cannot target a non-player")
    void cannotTargetNonPlayer() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        var creatureId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetControllerAndDoesNotCountResolvingPact() {
        List<Card> drawnCards = List.of(new Forest(), new Forest());
        harness.setGraveyard(player1, List.of(new Forest(), new EldritchPact()));
        harness.setGraveyard(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, drawnCards);
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        harness.assertLife(player1, 18);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        harness.assertLife(player2, 20);
    }

    @Test
    void emptyGraveyardDrawsNothingAndLosesNoLife() {
        Card libraryCard = new Forest();
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(libraryCard));
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryCard);
        harness.assertLife(player2, 20);
    }

    @Test
    void graveyardSizeIsDeterminedAtResolution() {
        List<Card> drawnCards = List.of(new Forest(), new Forest());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, drawnCards);
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);
        harness.castSorcery(player1, 0, player2.getId());

        harness.setGraveyard(player2, List.of(new Forest(), new EldritchPact()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrderElementsOf(drawnCards);
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({EldritchPact.class, Forest.class, ChainsOfMephistopheles.class})
    void drawReplacementChangingGraveyardDoesNotChangeLifeLoss() {
        harness.addToBattlefield(player1, new ChainsOfMephistopheles());
        harness.setGraveyard(player2, List.of(new Forest(), new EldritchPact()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new EldritchPact()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }
}
