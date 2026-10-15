package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.g.Groundskeeper;
import com.github.laxika.magicalvibes.cards.s.SoothingBalm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Putrefaction.class, Groundskeeper.class, FreshVolunteers.class, CloudSprite.class, Forest.class, SoothingBalm.class})
class PutrefactionTest extends BaseCardTest {

    @Test
    @DisplayName("A green spell makes its caster discard a card")
    void greenSpellMakesCasterDiscard() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player2, new ArrayList<>(List.of(new Groundskeeper(), new Forest())));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A white spell makes its caster discard a card")
    void whiteSpellMakesCasterDiscard() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player1, new ArrayList<>(List.of(new FreshVolunteers(), new Forest())));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("A spell of another color does not trigger Putrefaction")
    void otherColorDoesNotTrigger() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player1, new ArrayList<>(List.of(new CloudSprite(), new Forest())));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A matching spell does not prompt when its caster has no cards left")
    void matchingSpellWithEmptyHandDoesNotPrompt() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new Groundskeeper(), "{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Groundskeeper");
    }

    @Test
    @DisplayName("The caster chooses exactly one card before a white instant resolves")
    void casterChoosesOneCardBeforeInstantResolves() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player2, new ArrayList<>(List.of(
                new SoothingBalm(), new Forest(), new CloudSprite())));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.setLife(player1, 20);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.assertLife(player1, 20);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Cloud Sprite");
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();

        harness.passBothPriorities();

        harness.assertLife(player1, 25);
        harness.assertInGraveyard(player2, "Soothing Balm");
    }

    @Test
    @DisplayName("Each Putrefaction independently makes the caster discard")
    void multipleCopiesEachCauseDiscard() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player2, new ArrayList<>(List.of(
                new Groundskeeper(), new Forest(), new CloudSprite())));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
        harness.assertInGraveyard(player2, "Cloud Sprite");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Groundskeeper");
    }

    @Test
    @DisplayName("Playing a Forest is not casting a green spell")
    void playingLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Putrefaction());
        harness.setHand(player1, new ArrayList<>(List.of(new Forest(), new CloudSprite())));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Cloud Sprite");
        harness.assertOnBattlefield(player1, "Forest");
    }
}
