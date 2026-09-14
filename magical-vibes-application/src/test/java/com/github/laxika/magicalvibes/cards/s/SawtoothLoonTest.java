package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcticMerfolk;
import com.github.laxika.magicalvibes.cards.a.AuroraGriffin;
import com.github.laxika.magicalvibes.cards.q.QuirionExplorer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SawtoothLoon.class, AuroraGriffin.class, ArcticMerfolk.class, QuirionExplorer.class})
class SawtoothLoonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only allows a white or blue creature you control to be returned")
    void etbFiltersBounceChoices() {
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin()).getId();
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new ArcticMerfolk()).getId();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new QuirionExplorer()).getId();
        harness.addToBattlefield(player2, new ArcticMerfolk());

        castAndResolveSpell();

        UUID loonId = harness.getPermanentId(player1, "Sawtooth Loon");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(whiteId, blueId, loonId)
                .doesNotContain(greenId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB bounces first, then draws two and puts two cards on the bottom")
    void bouncesThenDrawsAndBottomsTwo() {
        UUID whiteId = harness.addToBattlefieldAndReturn(player1, new AuroraGriffin()).getId();
        Card first = new QuirionExplorer();
        Card second = new ArcticMerfolk();
        Card third = new QuirionExplorer();
        Card fourth = new AuroraGriffin();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castAndResolveSpell();
        harness.handlePermanentChosen(player1, whiteId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertInHand(player1, "Aurora Griffin");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(third, fourth, first, second);
        harness.assertOnBattlefield(player1, "Sawtooth Loon");
    }

    @Test
    @DisplayName("May return Sawtooth Loon itself before drawing and bottoming cards")
    void mayReturnItselfBeforeDrawing() {
        Card first = new QuirionExplorer();
        Card second = new ArcticMerfolk();
        Card third = new QuirionExplorer();
        Card fourth = new AuroraGriffin();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castAndResolveSpell();

        UUID loonId = harness.getPermanentId(player1, "Sawtooth Loon");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(loonId);
        harness.handlePermanentChosen(player1, loonId);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertInHand(player1, "Sawtooth Loon");
        harness.assertNotOnBattlefield(player1, "Sawtooth Loon");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(third, fourth, first, second);
    }

    private void castAndResolveSpell() {
        harness.castFromHand(player1, new SawtoothLoon(), "{2}{W}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
