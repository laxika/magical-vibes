package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TurnTheEarth.class, GrizzlyBears.class, LightningBolt.class})
class TurnTheEarthTest extends BaseCardTest {

    @Test
    void shufflesUpToThreeCardsFromDifferentGraveyardsAndGainsLife() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new LightningBolt();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        int ownLibrarySize = gd.playerDecks.get(player1.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());
        harness.setHand(player1, List.of(new TurnTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCard.getId(), opponentCard.getId());

        List<UUID> targets = List.of(ownCard.getId(), opponentCard.getId());
        harness.handleMultipleCardsChosen(player1, targets);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibrarySize + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(ownCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(opponentCard.getId()));
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void flashbackShufflesCardsFromDifferentGraveyardsAndExilesThisSpell() {
        Card ownCard = new GrizzlyBears();
        Card opponentCard = new LightningBolt();
        TurnTheEarth spell = new TurnTheEarth();
        harness.setGraveyard(player1, List.of(spell, ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        int ownLibrarySize = gd.playerDecks.get(player1.getId()).size();
        int opponentLibrarySize = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCard.getId(), opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId(), opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(ownLibrarySize + 1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibrarySize + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
    }

    @Test
    void hasFlashbackCost() {
        FlashbackCast flashback = new TurnTheEarth().getCastingOption(FlashbackCast.class).orElseThrow();

        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{1}{G}");
    }
}
