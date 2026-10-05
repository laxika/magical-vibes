package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ClingToDust;
import com.github.laxika.magicalvibes.cards.n.NyxbornCourser;
import com.github.laxika.magicalvibes.cards.s.ShatterTheSky;
import com.github.laxika.magicalvibes.cards.s.SoulGuideLantern;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeoninOfTheLostPride.class, NyxbornCourser.class, ShatterTheSky.class,
        SoulGuideLantern.class, ClingToDust.class})
class LeoninOfTheLostPrideTest extends BaseCardTest {

    @Test
    @DisplayName("When Leonin of the Lost Pride dies, it exiles a targeted card from an opponent's graveyard")
    void deathExilesOpponentGraveyardCard() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card courser = new NyxbornCourser();
        harness.setGraveyard(player2, new ArrayList<>(List.of(courser)));

        destroyLeonin();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(courser.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(courser.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(courser.getId()));
    }

    @Test
    @DisplayName("Only opponent graveyard cards are legal targets")
    void ownGraveyardCardIsNotTargetable() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card ownCard = new NyxbornCourser();
        Card opponentCard = new NyxbornCourser();
        harness.setGraveyard(player1, new ArrayList<>(List.of(ownCard)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        destroyLeonin();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(opponentCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(ownCard.getId());
    }

    @Test
    @DisplayName("No opponent graveyard card means the death trigger is not put on the stack")
    void noOpponentGraveyardCardSkipsTrigger() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new NyxbornCourser())));

        destroyLeonin();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    void mustChooseExactlyOneCardAndCanExileNoncreature() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card artifact = new SoulGuideLantern();
        Card creature = new NyxbornCourser();
        harness.setGraveyard(player2, List.of(artifact, creature));

        destroyLeonin();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
    }

    @Test
    void canTargetOpponentCreatureThatDiesSimultaneously() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card courser = new NyxbornCourser();
        harness.addToBattlefield(player2, courser);

        destroyLeonin();
        harness.handleMultipleCardsChosen(player1, List.of(courser.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(courser);
        harness.assertNotOnBattlefield(player2, "Nyxborn Courser");
        harness.assertInGraveyard(player1, "Leonin of the Lost Pride");
    }

    @Test
    void removedTargetIsNotReplacedByAnotherGraveyardCard() {
        harness.addToBattlefield(player1, new LeoninOfTheLostPride());
        Card target = new NyxbornCourser();
        Card other = new ShatterTheSky();
        harness.setGraveyard(player2, List.of(target, other));

        destroyLeonin();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player1, List.of(new ClingToDust()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentControlledLeoninTargetsOurGraveyard() {
        harness.addToBattlefield(player2, new LeoninOfTheLostPride());
        Card courser = new NyxbornCourser();
        harness.setGraveyard(player1, List.of(courser));

        destroyLeonin();
        harness.handleMultipleCardsChosen(player2, List.of(courser.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(courser);
        harness.assertInGraveyard(player2, "Leonin of the Lost Pride");
    }

    private void destroyLeonin() {
        harness.castFromHand(player1, new ShatterTheSky(), "{2}{W}{W}");
        harness.passBothPriorities();
    }
}
