package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnexpectedConversion.class, Shock.class, LightningBolt.class, Forest.class, Island.class})
class UnexpectedConversionTest extends BaseCardTest {

    @Test
    void exilesChosenCopiesAndSeeksForHandCopies() {
        Shock chosenCard = new Shock();
        Shock handCopy = new Shock();
        Shock libraryCopy = new Shock();
        LightningBolt soughtCard = new LightningBolt();

        harness.setHand(player1, List.of(new UnexpectedConversion(), chosenCard, handCopy));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), libraryCopy, soughtCard));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice handChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(handChoice).isNotNull();
        harness.handleCardChosen(player1, gd.playerHands.get(player1.getId()).indexOf(chosenCard));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(handCopy.getId(), libraryCopy.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(chosenCard, handCopy, libraryCopy);
        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(soughtCard);
    }

    @Test
    void mayDeclineTheInitialExile() {
        Shock cardToKeep = new Shock();
        LightningBolt remainingCard = new LightningBolt();
        harness.setHand(player1, List.of(new UnexpectedConversion(), cardToKeep));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), remainingCard));
        addMana();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(cardToKeep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingCard);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
