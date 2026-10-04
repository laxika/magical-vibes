package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExcavatingAnurid.class, SnowCoveredForest.class})
class ExcavatingAnuridTest extends BaseCardTest {

    @Test
    @DisplayName("May sacrifice a land when it enters to draw a card")
    void maySacrificeLandToDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        Card drawnCard = new SnowCoveredForest();
        harness.setLibrary(player1, List.of(drawnCard));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snow-Covered Forest");
        harness.assertInHand(player1, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("Declining the ETB ability does not sacrifice a land or draw")
    void decliningDoesNotSacrificeOrDraw() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Threshold gives it +1/+1 and vigilance")
    void thresholdBoostsAndGrantsVigilance() {
        harness.setGraveyard(player1, graveyardWithSevenCards());
        Permanent anurid = harness.addToBattlefieldAndReturn(player1, new ExcavatingAnurid());

        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Threshold does not apply below seven cards in its controller's graveyard")
    void noThresholdBelowSevenCards() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent anurid = harness.addToBattlefieldAndReturn(player1, new ExcavatingAnurid());

        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("No land to sacrifice means no card is drawn")
    void noLandDoesNotDraw() {
        harness.addToBattlefield(player2, new SnowCoveredForest());
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertOnBattlefield(player2, "Snow-Covered Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a land as the seventh card immediately enables threshold")
    void sacrificeEnablesThreshold() {
        harness.setGraveyard(player1, graveyardWithCards(6));
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SnowCoveredForest());
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        castAnurid(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        Permanent anurid = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() instanceof ExcavatingAnurid).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isTrue();
        harness.assertInHand(player1, "Snow-Covered Forest");
    }

    @Test
    @DisplayName("Threshold ends below seven cards and ignores the opponent's graveyard")
    void thresholdEndsAndIgnoresOpponentsGraveyard() {
        harness.setGraveyard(player1, graveyardWithCards(7));
        harness.setGraveyard(player2, graveyardWithCards(7));
        Permanent anurid = harness.addToBattlefieldAndReturn(player1, new ExcavatingAnurid());
        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(5);

        harness.setGraveyard(player1, graveyardWithCards(6));

        assertThat(gqs.getEffectivePower(gd, anurid)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, anurid)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, anurid, Keyword.VIGILANCE)).isFalse();
    }

    private void castAnurid(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player, new ExcavatingAnurid(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    private List<Card> graveyardWithSevenCards() {
        return graveyardWithCards(7);
    }

    private List<Card> graveyardWithCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new SnowCoveredForest());
        }
        return cards;
    }
}
