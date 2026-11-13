package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Riftsweeper.class, BlindPhantasm.class})
class RiftsweeperTest extends BaseCardTest {

    @Test
    @DisplayName("Can shuffle its controller's own exiled card into their library")
    void shufflesOwnExiledCardIntoLibrary() {
        BlindPhantasm exiledCard = new BlindPhantasm();
        harness.setExile(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, exiledCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Enters normally when exile contains no cards")
    void entersWithNoExiledCards() {
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Riftsweeper");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Does not move a target that has left exile before resolution")
    void doesNotMoveTargetThatLeftExile() {
        BlindPhantasm exiledCard = new BlindPhantasm();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, exiledCard.getId());
        assertThat(gd.stack).hasSize(1);

        assertThat(gd.removeFromExile(exiledCard.getId())).isTrue();
        harness.setHand(player2, List.of(exiledCard));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(exiledCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Shuffles a face-up exiled card into its owner's library")
    void shufflesFaceUpExiledCardIntoOwnersLibrary() {
        BlindPhantasm exiledCard = new BlindPhantasm();
        harness.setExile(player2, List.of(exiledCard));
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.AttackTriggerTarget.class);
        harness.handlePermanentChosen(player1, exiledCard.getId());

        harness.passBothPriorities();

        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(exiledCard);
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Does not target a face-down exiled card")
    void doesNotTargetFaceDownExiledCard() {
        BlindPhantasm exiledCard = new BlindPhantasm();
        gd.addToExile(player2.getId(), exiledCard, null, true);
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(exiledCard);
    }

    @Test
    @DisplayName("Offers only face-up exiled cards when a face-down card is also present")
    void offersOnlyFaceUpExiledCards() {
        BlindPhantasm faceUpCard = new BlindPhantasm();
        BlindPhantasm faceDownCard = new BlindPhantasm();
        harness.setExile(player2, List.of(faceUpCard));
        gd.addToExile(player2.getId(), faceDownCard, null, true);
        harness.setHand(player1, List.of(new Riftsweeper()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(faceUpCard.getId());
        harness.handlePermanentChosen(player1, faceUpCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(faceUpCard.getId())).isNull();
        assertThat(gd.findExiledCard(faceDownCard.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).contains(faceUpCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(faceDownCard);
    }
}
