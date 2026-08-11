package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LoamingShamanTest extends BaseCardTest {

    private void castShaman() {
        harness.setHand(player1, List.of(new LoamingShaman()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB targets a player before choosing cards from that graveyard")
    void targetsPlayerThenCards() {
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        harness.setGraveyard(player2, List.of(bears, bolt));

        castShaman();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), bolt.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Shuffles any number of cards from the targeted graveyard")
    void shufflesSelectedCardsFromTargetedGraveyard() {
        Card bears = new GrizzlyBears();
        Card bolt = new LightningBolt();
        Card remaining = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears, bolt, remaining));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());

        List<UUID> validIds = new ArrayList<>(
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds.subList(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
    }

    @Test
    @DisplayName("Choosing zero cards still shuffles the targeted player's library")
    void choosingZeroCardsLeavesGraveyardCardsInPlace() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        castShaman();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(bears);
        harness.assertOnBattlefield(player1, "Loaming Shaman");
    }

    @Test
    @DisplayName("Targeting a player with no graveyard cards needs no card-choice prompt")
    void emptyTargetGraveyardNeedsNoCardChoice() {
        castShaman();

        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Loaming Shaman");
    }
}
