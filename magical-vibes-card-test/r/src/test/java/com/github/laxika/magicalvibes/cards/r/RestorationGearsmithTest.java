package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CogworkersPuzzleknot;
import com.github.laxika.magicalvibes.cards.p.PrakhataClubSecurity;
import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RestorationGearsmith.class, CogworkersPuzzleknot.class, PrakhataClubSecurity.class, TidyConclusion.class})
class RestorationGearsmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB targets an artifact or creature card and returns it to hand")
    void etbReturnsArtifactOrCreatureToHand() {
        Card artifact = new CogworkersPuzzleknot();
        Card creature = new PrakhataClubSecurity();
        Card instant = new TidyConclusion();
        harness.setGraveyard(player1, List.of(instant, artifact, creature));

        castRestorationGearsmith();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(artifact.getId(), creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertInGraveyard(player1, "Cogworker's Puzzleknot");
        harness.assertInGraveyard(player1, "Tidy Conclusion");
    }

    @Test
    @DisplayName("ETB cannot target a card that is neither an artifact nor a creature")
    void etbCannotTargetOtherCardTypes() {
        harness.setGraveyard(player1, List.of(new TidyConclusion()));

        castRestorationGearsmith();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Tidy Conclusion");
    }

    @Test
    @DisplayName("ETB returns an artifact to hand without returning the other eligible card")
    void etbReturnsArtifactToHand() {
        Card artifact = new CogworkersPuzzleknot();
        Card creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(artifact, creature));

        castRestorationGearsmith();
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Cogworker's Puzzleknot");
        harness.assertNotInGraveyard(player1, "Cogworker's Puzzleknot");
        harness.assertNotOnBattlefield(player1, "Cogworker's Puzzleknot");
        harness.assertInGraveyard(player1, "Prakhata Club Security");
        harness.assertNotInHand(player1, "Prakhata Club Security");
    }

    @Test
    @DisplayName("ETB cannot target cards in an opponent's graveyard")
    void etbOnlyTargetsControllersGraveyard() {
        Card ownCard = new PrakhataClubSecurity();
        Card opponentsCard = new CogworkersPuzzleknot();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentsCard));

        castRestorationGearsmith();

        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactly(ownCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertInGraveyard(player2, "Cogworker's Puzzleknot");
        harness.assertNotInHand(player1, "Cogworker's Puzzleknot");
    }

    @Test
    @DisplayName("ETB has no target when only the opponent has eligible graveyard cards")
    void etbWithEmptyControllersGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new PrakhataClubSecurity()));

        castRestorationGearsmith();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Restoration Gearsmith");
        harness.assertInGraveyard(player2, "Prakhata Club Security");
        harness.assertNotInHand(player1, "Prakhata Club Security");
    }

    @Test
    @DisplayName("ETB does not choose a replacement when its target leaves the graveyard")
    void etbTargetLeavesGraveyardBeforeResolution() {
        Card target = new PrakhataClubSecurity();
        Card otherCard = new CogworkersPuzzleknot();
        harness.setGraveyard(player1, List.of(target, otherCard));

        castRestorationGearsmith();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherCard));
        harness.setExile(player1, List.of(target));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Prakhata Club Security");
        harness.assertInGraveyard(player1, "Cogworker's Puzzleknot");
        harness.assertNotInHand(player1, "Cogworker's Puzzleknot");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB requires exactly one target when eligible cards exist")
    void etbTargetSelectionIsMandatoryAndLimitedToOne() {
        Card artifact = new CogworkersPuzzleknot();
        Card creature = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(artifact, creature));

        castRestorationGearsmith();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(artifact.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertInGraveyard(player1, "Cogworker's Puzzleknot");
    }

    @Test
    @DisplayName("ETB resolves after Restoration Gearsmith leaves the battlefield")
    void etbResolvesAfterSourceIsDestroyed() {
        Card target = new PrakhataClubSecurity();
        harness.setGraveyard(player1, List.of(target));

        castRestorationGearsmith();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Restoration Gearsmith"));
        harness.assertInGraveyard(player1, "Restoration Gearsmith");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prakhata Club Security");
        harness.assertNotInGraveyard(player1, "Prakhata Club Security");
    }

    private void castRestorationGearsmith() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new RestorationGearsmith()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
