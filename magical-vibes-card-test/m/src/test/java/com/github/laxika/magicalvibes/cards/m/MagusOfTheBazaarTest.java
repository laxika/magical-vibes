package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.g.GossamerPhantasm;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagusOfTheBazaar.class, AvenRiftwatcher.class, GossamerPhantasm.class})
class MagusOfTheBazaarTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards, then discards three cards")
    void drawsTwoThenDiscardsThree() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheBazaar());
        harness.setHand(player1, List.of(new AvenRiftwatcher(), new GossamerPhantasm(), new AvenRiftwatcher()));
        harness.setLibrary(player1, List.of(new GossamerPhantasm(), new AvenRiftwatcher()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(5);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(magus.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("An empty starting hand still draws two and discards both")
    void emptyHandDiscardsAsManyAsPossible() {
        addCreatureReady(player1, new MagusOfTheBazaar());
        harness.setHand(player1, List.of());
        GossamerPhantasm firstDraw = new GossamerPhantasm();
        AvenRiftwatcher secondDraw = new AvenRiftwatcher();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Newly drawn cards can be chosen for the discard")
    void canDiscardNewlyDrawnCards() {
        addCreatureReady(player1, new MagusOfTheBazaar());
        AvenRiftwatcher retained = new AvenRiftwatcher();
        AvenRiftwatcher discarded = new AvenRiftwatcher();
        GossamerPhantasm firstDraw = new GossamerPhantasm();
        GossamerPhantasm secondDraw = new GossamerPhantasm();
        harness.setHand(player1, List.of(retained, discarded));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discarded, firstDraw, secondDraw);
    }

    @Test
    @DisplayName("A tapped Magus cannot activate the tap ability")
    void cannotActivateWhileTapped() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheBazaar());
        magus.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Magus cannot activate the tap ability")
    void cannotActivateWithSummoningSickness() {
        Permanent magus = addCreatureReady(player1, new MagusOfTheBazaar());
        magus.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(magus.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
