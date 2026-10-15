package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FeralProwler;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuarryBeetle.class, Forest.class, FeralProwler.class})
class QuarryBeetleTest extends BaseCardTest {

    /** Casts Quarry Beetle and resolves the creature spell so its ETB trigger sets up graveyard targeting. */
    private void castQuarryBeetle() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new QuarryBeetle(), "{4}{G}");
        harness.passBothPriorities(); // resolve creature → ETB triggers graveyard targeting
    }

    @Test
    @DisplayName("ETB returns a targeted land card from graveyard to the battlefield")
    void etbReturnsLandToBattlefield() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities(); // resolve the ETB triggered ability
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A nonland card in the graveyard is not a legal target")
    void nonlandNotTargetable() {
        harness.setGraveyard(player1, List.of(new FeralProwler()));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertInGraveyard(player1, "Feral Prowler");
    }

    @Test
    @DisplayName("The optional return can be declined")
    void returnCanBeDeclined() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Empty graveyard offers no target choice")
    void emptyGraveyardNoTrigger() {
        castQuarryBeetle();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A legal land target is mandatory even though returning it is optional")
    void targetMustBeChosenBeforeTheReturnDecision() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only land cards in the controller's graveyard are offered as targets")
    void targetPoolExcludesNonlandsAndOpponentsCards() {
        Forest ownLand = new Forest();
        Forest opposingLand = new Forest();
        harness.setGraveyard(player1, List.of(ownLand, new FeralProwler()));
        harness.setGraveyard(player2, List.of(opposingLand));

        castQuarryBeetle();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.cards()).containsExactly(ownLand);
    }

    @Test
    @DisplayName("A land removed from the graveyard before resolution is not returned")
    void removedTargetIsNotReturned() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        castQuarryBeetle();
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
