package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrosanReclamation.class, GrizzlyBears.class, LightningBolt.class})
class KrosanReclamationTest extends BaseCardTest {

    @Test
    void shufflesUpToTwoCardsFromTargetPlayersGraveyard() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new LightningBolt(), new GrizzlyBears()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> targetIds = new ArrayList<>(choice.validCardIds());
        harness.handleMultipleCardsChosen(player1, targetIds.subList(0, 2));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    void flashbackExilesSpellAfterResolution() {
        KrosanReclamation spell = new KrosanReclamation();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        int librarySizeBefore = gd.playerDecks.get(player2.getId()).size();
        addMana();

        harness.castFlashback(player1, 0, player2.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySizeBefore + 1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
