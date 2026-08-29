package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SawtoothLoonTest extends BaseCardTest {

    @Test
    @DisplayName("ETB only allows a white or blue creature you control to be returned")
    void etbFiltersBounceChoices() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        UUID greenId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.addToBattlefield(player2, new CloudSprite());

        castAndResolveSpell();

        UUID loonId = harness.getPermanentId(player1, "Sawtooth Loon");
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(blueId, loonId)
                .doesNotContain(greenId);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.BounceCreature.class);
    }

    @Test
    @DisplayName("ETB bounces first, then draws two and puts two cards on the bottom")
    void bouncesThenDrawsAndBottomsTwo() {
        UUID blueId = harness.addToBattlefieldAndReturn(player1, new CloudSprite()).getId();
        Card first = new GrizzlyBears();
        Card second = new Island();
        Card third = new GrizzlyBears();
        Card fourth = new Island();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        castAndResolveSpell();
        harness.handlePermanentChosen(player1, blueId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.assertInHand(player1, "Cloud Sprite");
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(third, fourth, first, second);
        harness.assertOnBattlefield(player1, "Sawtooth Loon");
    }

    private void castAndResolveSpell() {
        harness.setHand(player1, List.of(new SawtoothLoon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
