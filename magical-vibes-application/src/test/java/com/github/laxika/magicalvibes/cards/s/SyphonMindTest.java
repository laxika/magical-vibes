package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyphonMind.class, GlorySeeker.class, Dodecapod.class})
class SyphonMindTest extends BaseCardTest {

    @Test
    void opponentChoosesOneCardAndControllerKeepsTheirHand() {
        GlorySeeker controllerKept = new GlorySeeker();
        GlorySeeker opponentKept = new GlorySeeker();
        GlorySeeker discarded = new GlorySeeker();
        GlorySeeker drawn = new GlorySeeker();
        GlorySeeker undrawn = new GlorySeeker();
        harness.setHand(player1, List.of(new SyphonMind(), controllerKept));
        harness.setHand(player2, List.of(opponentKept, discarded));
        harness.setLibrary(player1, List.of(drawn, undrawn));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerKept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn, undrawn);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(controllerKept, drawn);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(opponentKept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void opponentDiscardsAndControllerDraws() {
        GlorySeeker drawn = new GlorySeeker();
        GlorySeeker discarded = new GlorySeeker();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromHand(player1, new SyphonMind(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Glory Seeker");
    }

    @Test
    void noCardDiscardedMeansNoDraw() {
        harness.setHand(player2, List.of());
        GlorySeeker drawn = new GlorySeeker();
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromHand(player1, new SyphonMind(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void replacedDiscardStillCountsForControllerDraw() {
        GlorySeeker drawn = new GlorySeeker();
        harness.setHand(player2, List.of(new Dodecapod()));
        harness.setLibrary(player1, List.of(drawn));

        harness.castFromHand(player1, new SyphonMind(), "{3}{B}");
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Dodecapod");
        harness.assertNotInGraveyard(player2, "Dodecapod");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
