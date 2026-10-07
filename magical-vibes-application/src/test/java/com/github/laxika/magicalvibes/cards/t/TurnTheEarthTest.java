package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
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
    @CardUsed({TurnTheEarth.class})
    void gainsLifeWithEmptyGraveyards() {
        TurnTheEarth spell = new TurnTheEarth();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
    }

    @Test
    @CardUsed({TurnTheEarth.class})
    void canChooseZeroTargetsWhenCardsAreAvailable() {
        Card available = new TurnTheEarth();
        harness.setGraveyard(player2, List.of(available));
        harness.setHand(player1, List.of(new TurnTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(available);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
    }

    @Test
    @CardUsed({TurnTheEarth.class})
    void shufflesThreeSelectedCardsAndLeavesUnselectedCard() {
        Card first = new TurnTheEarth();
        Card second = new TurnTheEarth();
        Card third = new TurnTheEarth();
        Card unselected = new TurnTheEarth();
        harness.setGraveyard(player2, List.of(first, second, third, unselected));
        harness.setHand(player1, List.of(new TurnTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(3);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySize + 3).contains(first, second, third).doesNotContain(unselected);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @CardUsed({TurnTheEarth.class})
    void stillGainsLifeAndShufflesRemainingLegalTarget() {
        Card removed = new TurnTheEarth();
        Card remaining = new TurnTheEarth();
        harness.setGraveyard(player2, List.of(removed, remaining));
        harness.setHand(player1, List.of(new TurnTheEarth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();
        int lifeBefore = gd.getLife(player1.getId());

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setExile(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySize + 1).contains(remaining).doesNotContain(removed);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(removed);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @CardUsed({TurnTheEarth.class})
    void doesNotGainLifeWhenAllChosenTargetsBecomeIllegal() {
        Card target = new TurnTheEarth();
        TurnTheEarth spell = new TurnTheEarth();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.getLife(player1.getId());
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(target));
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
    }

    @Test
    @CardUsed({TurnTheEarth.class})
    void flashbackWithNoTargetsGainsLifeAndExilesSpell() {
        TurnTheEarth spell = new TurnTheEarth();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());

        harness.castAndResolveFlashback(player1, 0, null);

        harness.assertLife(player1, lifeBefore + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }
}
