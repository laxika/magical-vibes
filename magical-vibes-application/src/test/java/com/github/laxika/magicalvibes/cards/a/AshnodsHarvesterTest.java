package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.d.Disfigure;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshnodsHarvester.class, ArgothianSprite.class, Disfigure.class})
class AshnodsHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles a target card from any graveyard")
    void attackExilesTargetCardFromAnyGraveyard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AshnodsHarvester());
        harvester.setSummoningSick(false);

        Card ownCard = new ArgothianSprite();
        Card opponentCard = new ArgothianSprite();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCard.getId(), opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).contains(ownCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId).containsExactly(opponentCard.getId());
    }

    @Test
    @DisplayName("Unearth returns Ashnod's Harvester with haste and exiles it at the next end step")
    void unearthReturnsWithHasteAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new AshnodsHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent harvester = findPermanent(player1, "Ashnod's Harvester");
        assertThat(harvester.getGrantedKeywords()).contains(Keyword.HASTE);
        harness.assertNotInGraveyard(player1, "Ashnod's Harvester");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ashnod's Harvester");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("Ashnod's Harvester"));
    }

    @Test
    void attackExilesOpponentOwnedCardIntoOpponentsExile() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AshnodsHarvester());
        harvester.setSummoningSick(false);
        Card target = new ArgothianSprite();
        harness.setGraveyard(player2, List.of(target));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Argothian Sprite");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getId).contains(target.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId).doesNotContain(target.getId());
    }

    @Test
    void attackCannotDeclineToChooseAnAvailableTarget() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AshnodsHarvester());
        harvester.setSummoningSick(false);
        harness.setGraveyard(player2, List.of(new ArgothianSprite()));

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void attackDoesNotExileAnotherCardWhenTargetLeavesGraveyard() {
        Permanent harvester = harness.addToBattlefieldAndReturn(player1, new AshnodsHarvester());
        harvester.setSummoningSick(false);
        Card target = new ArgothianSprite();
        Card other = new AshnodsHarvester();
        harness.setGraveyard(player2, List.of(target, other));

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of(other));
        harness.setHand(player2, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).extracting(Card::getId).containsExactly(other.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void unearthCannotBeActivatedDuringCombat() {
        harness.setGraveyard(player1, List.of(new AshnodsHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unearthedHarvesterIsExiledInsteadOfDying() {
        harness.setGraveyard(player1, List.of(new AshnodsHarvester()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent harvester = findPermanent(player1, "Ashnod's Harvester");
        harness.setHand(player1, List.of(new Disfigure()));

        harness.castInstant(player1, 0, harvester.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ashnod's Harvester");
        harness.assertNotInGraveyard(player1, "Ashnod's Harvester");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getId)
                .contains(harvester.getCard().getId());
    }
}
