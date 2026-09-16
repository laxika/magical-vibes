package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CarefulStudy;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PipersMelody.class, Werebear.class, CarefulStudy.class, GroundSeal.class})
class PipersMelodyTest extends BaseCardTest {

    @Test
    @DisplayName("Prompts for any number of target creature cards in your graveyard")
    void promptsForAnyNumberOfCreatureCards() {
        Card bears = new Werebear();
        Card spider = new Werebear();
        harness.setGraveyard(player1, List.of(bears, new CarefulStudy(), spider));
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactlyInAnyOrder(bears.getId(), spider.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shuffles the selected creature cards into your library")
    void shufflesSelectedCreatureCardsIntoLibrary() {
        Card bears = new Werebear();
        Card spider = new Werebear();
        Card nonCreature = new CarefulStudy();
        harness.setGraveyard(player1, List.of(bears, nonCreature, spider));
        int librarySize = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        List<UUID> creatureIds = List.of(bears.getId(), spider.getId());
        harness.handleMultipleCardsChosen(player1, creatureIds);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(bears.getId(), spider.getId());
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize + 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(nonCreature.getId())
                .doesNotContain(bears.getId(), spider.getId());
        harness.assertInGraveyard(player1, "Piper's Melody");
    }

    @Test
    @DisplayName("Selecting zero targets leaves the graveyard unchanged")
    void selectingZeroTargetsLeavesGraveyardUnchanged() {
        Card bears = new Werebear();
        harness.setGraveyard(player1, List.of(bears));
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Werebear");
        harness.assertInGraveyard(player1, "Piper's Melody");
    }

    @Test
    @DisplayName("With no creature cards in your graveyard the spell resolves without a prompt")
    void noCreatureCardsSkipPrompt() {
        Card nonCreature = new CarefulStudy();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Careful Study");
        harness.assertInGraveyard(player1, "Piper's Melody");
    }

    @Test
    @DisplayName("Does not target creature cards in an opponent's graveyard")
    void doesNotTargetOpponentsGraveyard() {
        Card ownCreature = new Werebear();
        Card opponentCreature = new Werebear();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opponentCreature));
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .contains(ownCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId)
                .containsExactly(opponentCreature.getId());
    }

    @Test
    @DisplayName("Ground Seal prevents a graveyard target prompt")
    void groundSealPreventsGraveyardTargetPrompt() {
        Card creature = new Werebear();
        harness.addToBattlefield(player1, new GroundSeal());
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new PipersMelody()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Werebear");
        harness.assertInGraveyard(player1, "Piper's Melody");
    }
}
