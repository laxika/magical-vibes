package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PutAwayTest extends BaseCardTest {

    private void player1CastsBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
    }

    private void giveP2PutAway() {
        harness.setHand(player2, List.of(new PutAway()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }

    @Test
    @DisplayName("Counters the spell and shuffles a chosen card from own graveyard into library")
    void countersAndShufflesChosenCard() {
        Card buried = new LightningBolt();
        harness.setGraveyard(player2, List.of(buried));
        int libSizeBefore = gd.playerDecks.get(player2.getId()).size();

        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        giveP2PutAway();
        harness.castInstant(player2, 0, bears.getId());

        // Put Away resolves: counters Grizzly Bears, then prompts player2 for a graveyard card
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        harness.handleGraveyardCardChosen(player2, 0);

        // Grizzly Bears was countered
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Lightning Bolt was shuffled out of the graveyard into the library
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Counters the spell; declining leaves the graveyard untouched")
    void countersAndDeclineLeavesGraveyard() {
        Card buried = new LightningBolt();
        harness.setGraveyard(player2, List.of(buried));
        int libSizeBefore = gd.playerDecks.get(player2.getId()).size();

        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        giveP2PutAway();
        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);

        // Decline the optional shuffle
        harness.handleGraveyardCardChosen(player2, -1);

        // Spell still countered
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Lightning Bolt stays in the graveyard; library unchanged
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libSizeBefore);
    }

    @Test
    @DisplayName("Counters the spell with an empty graveyard — no prompt")
    void countersWithEmptyGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        giveP2PutAway();
        harness.castInstant(player2, 0, bears.getId());

        harness.passBothPriorities();

        // No graveyard prompt, spell countered, Put Away in player2's graveyard
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Put Away"));
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a spell on the stack")
    void cannotTargetPermanent() {
        player1CastsBears();

        GrizzlyBears onBoard = new GrizzlyBears();
        harness.addToBattlefield(player1, onBoard);
        UUID permanentId = harness.getPermanentId(player1, "Grizzly Bears");

        giveP2PutAway();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, permanentId))
                .isInstanceOf(IllegalStateException.class);
    }
}
